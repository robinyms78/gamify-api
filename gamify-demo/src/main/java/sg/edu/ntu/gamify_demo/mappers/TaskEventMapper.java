package sg.edu.ntu.gamify_demo.mappers;

import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

public interface TaskEventMapper {
    TaskEventDTO toDTO(TaskEvent taskEvent);
}
