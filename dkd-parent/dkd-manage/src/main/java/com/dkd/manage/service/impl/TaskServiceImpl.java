package com.dkd.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dkd.common.constant.DkdContants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Emp;
import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.TaskDetails;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.dto.TaskDTO;
import com.dkd.manage.domain.dto.TaskDetailsDTO;
import com.dkd.manage.domain.vo.TaskVO;
import com.dkd.manage.mapper.TaskMapper;
import com.dkd.manage.service.IEmpService;
import com.dkd.manage.service.ITaskDetailsService;
import com.dkd.manage.service.ITaskService;
import com.dkd.manage.service.IVendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工单Service业务层处理
 *
 * @author itheima
 * @date 2025-02-11
 */
@Service
public class TaskServiceImpl implements ITaskService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private IVendingMachineService vendingMachineService;
    @Autowired
    private IEmpService empService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ITaskDetailsService iTaskDetailsService;

    /**
     * 查询工单
     *
     * @param taskId 工单主键
     * @return 工单
     */
    @Override
    public Task selectTaskByTaskId(Long taskId) {
        return taskMapper.selectTaskByTaskId(taskId);
    }

    /**
     * 查询工单列表
     *
     * @param task 工单
     * @return 工单
     */
    @Override
    public List<Task> selectTaskList(Task task) {
        return taskMapper.selectTaskList(task);
    }

    /**
     * 新增工单
     *
     * @param task 工单
     * @return 结果
     */
    @Override
    public int insertTask(Task task) {
        task.setCreateTime(DateUtils.getNowDate());
        return taskMapper.insertTask(task);
    }

    /**
     * 修改工单
     *
     * @param task 工单
     * @return 结果
     */
    @Override
    public int updateTask(Task task) {
        task.setUpdateTime(DateUtils.getNowDate());
        return taskMapper.updateTask(task);
    }

    /**
     * 批量删除工单
     *
     * @param taskIds 需要删除的工单主键
     * @return 结果
     */
    @Override
    public int deleteTaskByTaskIds(Long[] taskIds) {
        return taskMapper.deleteTaskByTaskIds(taskIds);
    }

    /**
     * 删除工单信息
     *
     * @param taskId 工单主键
     * @return 结果
     */
    @Override
    public int deleteTaskByTaskId(Long taskId) {
        return taskMapper.deleteTaskByTaskId(taskId);
    }

    /**
     * 查询工单列表
     *
     * @param task
     * @return TaskVo集合
     */
    @Override
    public List<TaskVO> selectTaskVOList(Task task) {
        return taskMapper.selectTaskVOList(task);
    }

    /**
     * 新增运维、运营工单
     *
     * @param taskDTO
     * @return
     */
    @Transactional
    @Override
    public int insertTaskDTO(TaskDTO taskDTO) {
        // 1. 查询售货机是否存在
        VendingMachine vm = vendingMachineService.selectVendingMachineByInnerCode(taskDTO.getInnerCode());
        if (vm == null) {
            throw new ServiceException("此售货机不存在,请重新输入售货机编号");
        }
        // 2. 校验售货机状态与工单类型是否相符
        checkTaskType(vm.getVmStatus(), taskDTO.getProductTypeId());
        // 3. 检查设备是否有未完成的同类型工单
        hasTask(taskDTO);
        // 4. 查询并校验员工是否存在
        Emp emp = empService.selectEmpById(taskDTO.getUserId());
        if (emp == null) {
            throw new ServiceException("此员工不存在");
        }
        // 5.校验员工区域是否匹配
        if (!emp.getRegionId().equals(vm.getRegionId())) {
            throw new ServiceException("员工区域与售货机区域不匹配，无法处理此工单");
        }
        // 6.将dto转换为po并补充属性,保存工单
        Task task = BeanUtil.copyProperties(taskDTO, Task.class); // 属性复制
        task.setTaskStatus(DkdContants.TASK_STATUS_CREATE);// 创建工单
        task.setUserName(emp.getUserName());// 执行人名称
        task.setRegionId(vm.getRegionId());// 所属区域id
        task.setAddr(vm.getAddr());// 地址
        task.setCreateTime(DateUtils.getNowDate());// 创建时间
        task.setTaskCode(generateTaskCode());// 工单编号
        int taskResult = taskMapper.insertTask(task);
        // 7.判断是否为补货工单
        if (taskDTO.getProductTypeId().equals(DkdContants.TASK_TYPE_SUPPLY)) {
            // 8.保存工单详情
            List<TaskDetailsDTO> details = taskDTO.getDetails();
            if (CollUtil.isEmpty(details)) {
                throw new ServiceException("补货工单详情不能为空");
            }
            // 将dto转为po补充属性
            List<TaskDetails> taskDetailsList = details.stream().map(dto -> {
                TaskDetails taskDetails = BeanUtil.copyProperties(dto, TaskDetails.class);
                taskDetails.setTaskId(task.getTaskId());
                return taskDetails;
            }).collect(Collectors.toList());
            // 批量新增
            iTaskDetailsService.batchInsertTaskDetails(taskDetailsList);
        }
        return taskResult;
    }

    /**
     * 取消工单
     *
     * @param task
     * @return 取消结果
     */
    @Override
    public int cancelTask(Task task) {
        // 1.判断工单状态是否可以取消
        // 根据工单id查询数据库
        Task taskDb = taskMapper.selectTaskByTaskId(task.getTaskId());
        // 判断工单状态是否为已取消，如果是，则抛出异常
        if (taskDb.getTaskStatus().equals(DkdContants.TASK_STATUS_CANCEL)) {
            throw new ServiceException("此工单已取消，请勿重复操作");
        }
        // 判断工单状态是否为已完成，如果是，则抛出异常
        if (taskDb.getTaskStatus().equals(DkdContants.TASK_STATUS_FINISH)) {
            throw new ServiceException("此工单已完成，无法取消");
        }
        //2.设置更新字段
        task.setTaskStatus(DkdContants.TASK_STATUS_CANCEL); //工单状态：取消
        task.setUpdateTime(DateUtils.getNowDate());//更新时间
        return taskMapper.updateTask(task);
    }

    // 生成并获取当天工单编号（唯一标识）
    private String generateTaskCode() {
        // 获取当前日期并格式化为"yyyyMMdd"
        String dateStr = DateUtils.getDate().replaceAll("-", "");
        // 根据日期生成redis的键
        String key = "dkd.task.code" + dateStr;
        // 判断key是否存在
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            // 如果key不存在，设置初始值为1，并指定过期时间为1天
            redisTemplate.opsForValue().set(key, 1, Duration.ofDays(1));
            // 返回工单编号（日期+0001）
            return dateStr + "0001";
        }
        // 如果key存在，计数器+1(0002),确保字符串长度为4位
        // 返回工单编号
        return dateStr + StrUtil.padPre(Objects.requireNonNull(redisTemplate.opsForValue().increment(key)).toString(), 4, '0');
    }

    // 检查设备是否有未完成的同类型工单
    private void hasTask(TaskDTO taskDTO) {
        // 创建task条件对象，并设置设备编号和工单类型，以及工单状态为进行中
        Task taskParam = new Task();
        taskParam.setInnerCode(taskDTO.getInnerCode());
        taskParam.setProductTypeId(taskDTO.getProductTypeId());
        taskParam.setTaskStatus(DkdContants.TASK_STATUS_PROGRESS);
        // 调用taskMapper查询数据库查看是否有符合条件的工单列表
        List<Task> taskList = taskMapper.selectTaskList(taskParam);
        // 如果存在未完成的同类型工单，则抛出异常
        if (taskList != null && !taskList.isEmpty()) {
            throw new ServiceException("该设备已有未完成的工单，不能重复创建");
        }
    }

    // 校验售货机状态与工单类型是否相符
    private void checkTaskType(Long vmStatus, Long productTypeId) {
        // 如果是投放工单，设备在运行中，抛出异常
        if (productTypeId.equals(DkdContants.TASK_TYPE_DEPLOY) && vmStatus.equals(DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException("此售货机正在运行中，无法进行投放");
        }
        // 如果是维修工单，设备不在运行中，抛出异常
        if (productTypeId.equals(DkdContants.TASK_TYPE_REPAIR) && !vmStatus.equals(DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException("此售货机不在运行中，无法进行维修");
        }
        // 如果是补货工单，设备不在运行中，抛出异常
        if (productTypeId.equals(DkdContants.TASK_TYPE_SUPPLY) && !vmStatus.equals(DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException("此售货机不在运行中，无法进行补货");
        }
        // 如果是撤机工单，设备不在运行中，抛出异常
        if (productTypeId.equals(DkdContants.TASK_TYPE_REVOKE) && !vmStatus.equals(DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException("此售货机不在运行中，无法进行撤机");
        }
    }
}
