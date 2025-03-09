package sg.edu.ntu.gamify_demo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

@Mapper(componentModel = "spring")
public interface TaskEventMapper {

    @Mapping(source = "user.id", target = "userId")
    TaskEventDTO toDTO(TaskEvent taskEvent);
}
