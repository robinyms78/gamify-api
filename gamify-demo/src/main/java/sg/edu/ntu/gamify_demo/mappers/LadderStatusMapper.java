package sg.edu.ntu.gamify_demo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;

@Mapper
public interface LadderStatusMapper {
    LadderStatusMapper INSTANCE = Mappers.getMapper(LadderStatusMapper.class);

    @Mapping(source = "currentLevel", target = "currentLevel")
    @Mapping(source = "currentLevel.label", target = "levelLabel")
    @Mapping(source = "earnedPoints", target = "earnedPoints")
    @Mapping(source = "pointsToNextLevel", target = "pointsToNextLevel")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    LadderStatusDTO toDTO(UserLadderStatus userLadderStatus);
    
    default Integer map(LadderLevel ladderLevel) {
        return ladderLevel != null ? ladderLevel.getLevel().intValue() : null;
    }
}
