package sg.edu.ntu.gamify_demo.mappers;

import org.apache.ibatis.annotations.Mapper;
import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

@Mapper
public interface TaskEventMapper {
    TaskEventDTO toDTO(TaskEvent taskEvent);
}
