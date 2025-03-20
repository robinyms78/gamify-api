package sg.edu.ntu.gamify_demo.mappers;

import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

@Component
public class TaskEventMapperImpl implements TaskEventMapper {

    @Override
    public TaskEventDTO toDTO(TaskEvent taskEvent) {
        if (taskEvent == null) {
            return null;
        }

        TaskEventDTO taskEventDTO = new TaskEventDTO();
        
        taskEventDTO.setEventId(taskEvent.getEventId());
        taskEventDTO.setUserId(taskEvent.getUser() != null ? taskEvent.getUser().getId() : null);
        taskEventDTO.setTaskId(taskEvent.getTaskId());
        taskEventDTO.setEventType(taskEvent.getEventType());
        taskEventDTO.setStatus(taskEvent.getStatus());
        taskEventDTO.setCompletionTime(taskEvent.getCompletionTime() != null ? 
                                      taskEvent.getCompletionTime().toLocalDateTime() : null);
        
        return taskEventDTO;
    }
}
