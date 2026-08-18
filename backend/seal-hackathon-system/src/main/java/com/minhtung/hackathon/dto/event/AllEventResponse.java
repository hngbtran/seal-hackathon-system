package com.minhtung.hackathon.dto.event;

import com.minhtung.hackathon.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllEventResponse {
    private long eventId;
    private String eventName;
    private String eventTopic;
    private int maxTeamMember;
    private int maxTeam;
    private int teamQuantity=0;
    private int trackQuantity=0;
    private int candidateQuantity=0;
    private List<RoundLocationItem> eventLocations; // đổi từ String -> List, mỗi round 1 địa điểm
    private long prize;
    private String eventStatus;
    private int roundQuantity=0;
    private String description;
    private String thumbnail;
    private String[] keywords;
    private String rules;
    private List<EventNoteItem> notes;
    private List<MilestoneItemResponse> milestones;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class MilestoneItemResponse {
        private long id;
        private String milestoneName;
        private LocalDateTime dateStart;
        private LocalDateTime dateEnd;
        private String des;
        private String status; // UPCOMING, IN_PROGRESS, COMPLETED...
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class RoundLocationItem {
        private long roundId;
        private String roundName;
        private String locationName;
        private String detailLocation;
        private String position;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class EventNoteItem {
        private long id;
        private String title;
        private String description;
    }
}