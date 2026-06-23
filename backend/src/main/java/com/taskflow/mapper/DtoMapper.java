package com.taskflow.mapper;

import com.taskflow.dto.ActivityDto;
import com.taskflow.dto.BoardDtos.MemberDto;
import com.taskflow.dto.CardDtos.CardDto;
import com.taskflow.dto.ListDtos.ListDto;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.Activity;
import com.taskflow.entity.BoardMember;
import com.taskflow.entity.Card;
import com.taskflow.entity.TaskList;
import com.taskflow.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoMapper {

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getEmail(), user.getDisplayName());
    }

    public MemberDto toMemberDto(BoardMember member) {
        return new MemberDto(member.getId(), toUserDto(member.getUser()), member.getRole());
    }

    public CardDto toCardDto(Card card) {
        return new CardDto(
                card.getId(),
                card.getList().getId(),
                card.getTitle(),
                card.getDescription(),
                card.getPosition(),
                toUserDto(card.getAssignee()),
                card.getLabels(),
                card.getDueDate(),
                card.getCreatedAt(),
                card.getUpdatedAt());
    }

    public ListDto toListDto(TaskList list, List<Card> cards) {
        return new ListDto(
                list.getId(),
                list.getName(),
                list.getPosition(),
                cards.stream().map(this::toCardDto).toList());
    }

    public ActivityDto toActivityDto(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                toUserDto(activity.getActor()),
                activity.getMessage(),
                activity.getCreatedAt());
    }
}
