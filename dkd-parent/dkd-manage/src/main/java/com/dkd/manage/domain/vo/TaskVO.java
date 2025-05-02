package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.TaskType;
import lombok.Data;

/**
 * ClassName: TaskVO
 * Package: com.dkd.manage.domain.vo
 *
 * @Author: itheima-ht
 * @Create: 2025/2/13 21:11
 */
@Data
public class TaskVO extends Task {

    //工单类型
    private TaskType taskType;
}
