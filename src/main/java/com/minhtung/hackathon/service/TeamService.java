package com.minhtung.hackathon.service;


import com.minhtung.hackathon.dto.CreateTeamDto;
import com.minhtung.hackathon.dto.TeamResponseDto;
import com.minhtung.hackathon.entity.Member;
import com.minhtung.hackathon.entity.Team;
import com.minhtung.hackathon.entity.TeamRequest;
import com.minhtung.hackathon.entity.User;
import com.minhtung.hackathon.enums.MemberRole;
import com.minhtung.hackathon.enums.RequestStatus;
import com.minhtung.hackathon.enums.RequestType;
import com.minhtung.hackathon.enums.TeamStatus;
import com.minhtung.hackathon.repository.MemberRepository;
import com.minhtung.hackathon.repository.TeamRepository;
import com.minhtung.hackathon.repository.TeamRequestRepository;
import com.minhtung.hackathon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamRequestRepository teamRequestRepository;
    private final UserRepository userRepository;



    @Transactional
    public TeamResponseDto createTeam(CreateTeamDto create, long leaderId) {
        if (create.getTeamName() == null || create.getTeamName().trim().isEmpty())
            throw new RuntimeException("Tên đội không được để trống");
        if (create.getTeamName().length() > 30)
            throw new RuntimeException("Tên đội không thể lớn hơn 30 ký tự");

        User leader = userRepository.findById(leaderId).orElse(null);
        if(leader == null){
            throw new RuntimeException("Không tìm thấy user");
        }

        if (memberRepository.existsByMember_IdAndStatus(leaderId, true)) {
            throw new RuntimeException("Bạn đã thuộc team rồi, không thể tạo team mới");
        }
        String inviteCode = generateUniqueInviteCode();
        Team team = new Team();
        team.setName(create.getTeamName().trim());
        team.setLeader(leader);
        team.setInviteCode(inviteCode);
        team.setStatus(TeamStatus.OPEN);
        teamRepository.save(team);

        Member leaderMember = new Member() ;
        leaderMember.setRole(MemberRole.LEADER);
        leaderMember.setStatus(true);
        leaderMember.setTeam(team);
        leaderMember.setMember(leader);
        memberRepository.save(leaderMember);

        List<Long> memberIds = create.getMemberId();
        if (memberIds != null) {
            int limit = Math.min(memberIds.size(), 3); // duoc toi da 3 nguoi thoi vi leader la 1 roi
            for (int i = 0; i < limit; i++) {
                Long memberId = memberIds.get(i);
                if (memberId == null || memberId.equals(leaderId)) continue;

                // Kiểm tra user tồn tại
                User invitedUser = userRepository.findById(memberId).orElse(null);
                if (invitedUser == null) continue;

                // Kiểm tra user đã có đội chưa
                if (memberRepository.existsByMember_IdAndStatus(memberId, true)) continue;


                TeamRequest request =new TeamRequest();
                request.setSender(leader);
                request.setReceiver(invitedUser);
                request.setTeam(team);
                request.setType(RequestType.INVITATION) ;
                request.setStatus(RequestStatus.PENDING);

                teamRequestRepository.save(request);
            }
        }

        int memberCount = memberRepository.countByTeam_IdAndStatus(team.getId(), true);
        return toDto(team, memberCount);
    }





        //2. day la ham tham gia doi bang loi moi
        //luat la chi doi dang co Open moi co the join bang ma
        // tu dong them member ma khong can admin duyet


    @Transactional
    public String joinTeamByCode(String inviteCode,Long userId) {
        Team team = teamRepository.findByInviteCode(inviteCode).orElse(null);
        if (team == null) {
            return "Mã mời  không  tồn tại";

        }

        // nhung team nao open thi moi tham gia vao
        if (team.getStatus() != TeamStatus.OPEN) {
            return "đội nay không còn nhận thành viên nữa" + team.getStatus() + ") ";
        }


        User user = userRepository.findById(userId).orElse(null);
        if(user == null){
            return "khong tim thay user" ;
        }


        //kiem tra xem ban co thuoc team nao chua
        if (memberRepository.existsByTeam_IdAndMember_IdAndStatus(team.getId(), userId, true)) {
            ;

            return "ban da thuoc doi khac roi" + team.getName();
        }
        //yeu cau roi doi truoc khi muon tham gia
        if (memberRepository.existsByMember_IdAndStatus(userId,true)) {
            return "ban nen roi doi " + team.getName() + "truoc khi tham gia ";

        }

        int currentCount = memberRepository.countByTeam_IdAndStatus(team.getId(),true);
        if(currentCount >=4){
            return "doi da du 4 thanh vien roi" ;
        }

        Member member = new Member() ;
        member.setRole(MemberRole.MEMBER);
        member.setStatus(true);
        member.setTeam(team);
        member.setMember(user);
        memberRepository.save(member) ;



        return "tham gia doi " + team.getName() +"thanh cong";



    }

    //ham nay de kiem xem doi nao damg tuyen thanh vien(chi doi dang o trang thai open moi duoc xuát hien
    public List<TeamResponseDto>searchTeam(String keyword){
        if(keyword ==null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
            List<Team> teams = teamRepository.findByNameContainingIgnoreCaseAndStatus(keyword.trim(),TeamStatus.OPEN);
            List<TeamResponseDto> result = new ArrayList<>();
            for(Team team:teams){
                int count = memberRepository.countByTeam_IdAndStatus(team.getId(), true);
                TeamResponseDto dto = toDto(team,count);
                dto.setInviteCode(null);//de an ma moi khi earch cong khai

                result.add(dto);
            }
        return result ;
        }


        //ham nay de gui join request
      // dieu kien nhung team dang o trang thai open moi co
    //can leader duyet nưa nha

        @Transactional
        public  String sendJoinRequest(Long teamId,Long userId) {
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team == null) {
                return "không tim thấy đội ";
            }
            if (team.getStatus() != TeamStatus.OPEN) {
                return "đội nay không còn nhận thành viên nữa";

            }
            User user = userRepository.findById(userId).orElse(null);
            if(user == null){
                return "không tìm thấy user này " ;
            }
            if (memberRepository.existsByTeam_IdAndMember_IdAndStatus(teamId, userId, true)) {
                ;
                return "ban da o trong doi khac roi";
            }
            if (memberRepository.countByTeam_IdAndStatus(teamId, true) >= 4) {
                return "doi nay da du so luong thanh vien la 4";

            }
            //khuc nay la do leader duyet
            if (teamRequestRepository.existsBySender_IdAndTeam_IdAndTypeAndStatus(userId, teamId, RequestType.JOIN_REQUEST, RequestStatus.PENDING)) {
                ;
                return " bạn đã  yeu cau  leader duyệt";

            }
            if(teamRequestRepository.existsBySender_IdAndTypeAndStatus(userId,RequestType.JOIN_REQUEST,RequestStatus.PENDING)){
                return "Bạn đã có yêu cầu xin vào đội đang chờ duyệt, không thể gửi thêm";
            };



            TeamRequest request = new TeamRequest();
            request.setSender(user);
            request.setTeam(team);
            request.setReceiver(team.getLeader());
            request.setType(RequestType.JOIN_REQUEST);
            request.setStatus(RequestStatus.PENDING);
            teamRequestRepository.save(request);
            return "da gui yeu cau tham gia doi " + team.getName() +"cho leader duyet" ;


        }
        //User dong ý / từ chối lời mời (invation)
    //nguoi duoc moi co the tu xu ly

    @Transactional
    public String respondToInvitation(Long requestId , boolean acp , long userId) {
        TeamRequest req = teamRequestRepository.findById(requestId).orElse(null);
        if (req == null) {
            return "Không tìm thấy lời mời";
        }
        if (!req.getReceiver().getId().equals(userId)) {
            return "ban không phải người nhận lời mời";
        }
        if (req.getStatus() != RequestStatus.PENDING) {
            return "loi moi da duoc xu ly roi";

        }


        Team team = req.getTeam();
        if (!acp) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "da tu choi loi moi";
        }
        if (team == null || team.getStatus() != TeamStatus.OPEN) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "doi khong con nhan thanh vien nua" ;

        }
        if (memberRepository.countByTeam_IdAndStatus(team.getId(), true) >= 4) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "doi cua bạn da du va khong con nhan thanh vien nua";
        }
        if (memberRepository.existsByMember_IdAndStatus(userId, true)) {
            return "ban thuoc team khac roi can out de vao nhom khac";
        }
        Member member = new Member();
        member.setTeam(team);
        member.setRole(MemberRole.MEMBER);
        member.setStatus(true);
        member.setMember(req.getReceiver());
        memberRepository.save(member) ;
      return "đã tham gia đội thành công " ;




    }

    //Leader duyet / tu choi join request
    @Transactional
    public String respondToJoinRequest(Long requesrId , boolean acp , long leaderId) {
        TeamRequest req = teamRequestRepository.findById(requesrId).orElse(null);
        if (req == null) {
            return "khong tim thay yeu cau";
        }
        if (req.getType() != RequestType.JOIN_REQUEST) {
            return "day khong phai yeu cau xin vao doi ";
        }
        if (req.getReceiver() == null || !req.getReceiver().getId().equals(leaderId)) {
            return "ban khong phai leader";
        }
        if (req.getStatus() != RequestStatus.PENDING)
            return "Yeu cau nay da duoc xu ly roi ";


        Team team = req.getTeam();
        if (!acp) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "yeu cau nay da bi tu choi";
        }
        int count = memberRepository.countByTeam_IdAndStatus(requesrId, true);
        if (count >= 4) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "doi cua ban da du 4 thanh vien roi ";
        }
        if (memberRepository.existsByMember_IdAndStatus(req.getSender().getId(), true)) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "User nay thuoc team khac roi ";

        }
        Member member = new Member();
        member.setRole(MemberRole.MEMBER);
        member.setTeam(team);
        member.setStatus(true);
        member.setMember(req.getSender());
        memberRepository.save(member);
        req.setStatus(RequestStatus.APPROVED);
        teamRequestRepository.save(req);
        return "da duyet thanh vien vo doi ";
    }


    //thanh vie  gui leave_request xin roi doi
    //leader can duyet
    //leader khong the tu y roi doi (giai tán nhóm)
    @Transactional
    public String requestLeave(Long teamID , Long userId) {
        Member member = memberRepository.findByTeam_IdAndMember_Id(teamID, userId).orElse(null);
        if (member == null || !member.isStatus()) {
            return "ban khong thuoc doi nay ";
        }
        if (member.getRole() == MemberRole.LEADER) {
            return "ban la leader nen khong the roi doi duoc ";
        }
        Team team = member.getTeam();
        if (team == null) {
            return "khong tim thay doi nay";
        }
        if (team.getStatus() == TeamStatus.APPROVED) {
            return "đội của bạn đã được duyệt rôi , không thể rời đội";
        }
        if (teamRequestRepository.existsBySender_IdAndTeam_IdAndTypeAndStatus(userId, teamID, RequestType.LEAVE_REQUEST, RequestStatus.PENDING)) {

            return "bạn đã gửi yêu  cầu xin ròi nhóm , cần được leader duyệt ";
        }
        TeamRequest teamRequest = new TeamRequest();
        teamRequest.setSender(member.getMember());
        teamRequest.setReceiver(team.getLeader());
        teamRequest.setTeam(team);
        teamRequest.setType(RequestType.LEAVE_REQUEST);
        teamRequest.setStatus(RequestStatus.PENDING);
        teamRequestRepository.save(teamRequest);

        return "đã gửi yêu cầu rời đội , chờ leader duyệt ";

    }

        //8 day la ham dung de leadder duyet vuec leave_request trong team

    @Transactional
    public String respondToLeaveRequest(Long requestId , boolean acp , long leaderId){
        TeamRequest req = teamRequestRepository.findById(requestId).orElse(null);
        if(req == null){
            return "không tìm thấy yêu câu rời đội" ;
        }
        if(req.getType() !=  RequestType.LEAVE_REQUEST){
            return "đây không phải yêu cầu rời đội" ;
        }
        if(!req.getReceiver().getId().equals(leaderId)){
            return "ban khong phai leader cua doi nay" ;
        }
        if(req.getStatus() != RequestStatus.PENDING){
            return "yeu cau nay da duoc xu ly roi" ;
        }
        if(!acp) {
            req.setStatus(RequestStatus.REJECTED);
            teamRequestRepository.save(req);
            return "Đã từ chối yêu cầu rời đội";
        }
        Member member = memberRepository.findByTeam_IdAndMember_Id( req.getTeam().getId(), req.getSender().getId() ).orElse(null);
            if(member != null){
                member.setStatus(false);//danh dau da duoc roi doi
                memberRepository.save(member);
            }
            req.setStatus(RequestStatus.APPROVED);

        teamRequestRepository.save(req);
        return "Đã cho phép thành viên rời đội";
        }




    // leader chot doi  gui sumbmission cho ban admin
    //team status open thanh pending -approval
    @Transactional
    public  String submitTeamForApporal(long teamId , long leaderId){
        Team team = teamRepository.findById(teamId).orElse(null);
        if(team == null){
            return "khong tim thay doi" ;
        }
        if (team.getLeader() == null || !team.getLeader().getId().equals(leaderId)) {
            return "Chỉ Leader mới có quyền chốt đội";
        }
        if(team.getStatus() !=TeamStatus.OPEN){
            return "doi phai o trang thai open thi moi choi duoc ";
        }

        int count = memberRepository.countByTeam_IdAndStatus(teamId,true);
        if(count != 4 ){
            return "đội phải đủ 4 người mới được submit" ;
        }

        boolean alreadySumbit = teamRequestRepository.existsBySender_IdAndTeam_IdAndTypeAndStatus(leaderId,teamId,RequestType.TEAM_SUBMISSION,RequestStatus.PENDING);

        if(alreadySumbit) {
            return "doi dang cho admin duyet ";
        }
        team.setStatus(TeamStatus.PENDING_APPROVAL);
        teamRepository.save(team);
        TeamRequest request = new TeamRequest();
        request.setSender(team.getLeader());
        request.setReceiver(null);
        request.setTeam(team);
        request.setType(RequestType.TEAM_SUBMISSION);
        request.setStatus(RequestStatus.PENDING);
        teamRequestRepository.save(request);
        return "Đã nộp đội " + team.getName() + ", đang chờ admin duyệt";
    }



    //Admin duyet / tu choi team submisson
    //co 2 truong hop 1 appoved(khoa doi ) da dc duyey
   //hop 1 rejected(can chinh sua doi ) chua duoc duyet
    @Transactional
    public String adminReviewTeam(long requestId,boolean approve){
        TeamRequest req = teamRequestRepository.findById(requestId).orElse(null);
        if(req == null){
            return "khong tim thay yeu cau " ;
        }
        if(req.getType() != RequestType.TEAM_SUBMISSION){
            return "day khong phai yeu cau phe duyet " ;
        }
        if(req.getStatus() != RequestStatus.PENDING){
            return "yeu cau nay da duoc xu ly" ;
        }
        Team team = req.getTeam();
        if(team == null){
            return "khong tim thay doi" ;

        }
        if(approve){
            team.setStatus(TeamStatus.APPROVED);//duyet r nen khong cho khoa nua
            team.setInviteCode(null);//vo hieu hoa ma moi de khong cho ai dung lai ma moi nay
            req.setStatus(RequestStatus.APPROVED);
        }else{
            team.setStatus(TeamStatus.REJECTED);   // Admin từ chối
            req.setStatus(RequestStatus.REJECTED);

        }
        teamRepository.save(team);
        teamRequestRepository.save(req);

        return approve
                ? "Đội \"" + team.getName() + "\" đã được DUYỆT chính thức!"
                : "Đã TỪ CHỐI đội \"" + team.getName() + "\"";
    }





//tao ma random a

    private String generateUniqueInviteCode(){
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random() ;
        String code ;
        do{
            StringBuilder sb = new StringBuilder(6);
            for(int i = 0 ; i<6 ; i++)
                sb.append(chars.charAt((random.nextInt(chars.length()))));
                code = sb.toString();

        }while (teamRepository.existsByInviteCode(code));
        return code ;
    }

    private TeamResponseDto toDto(Team team, int memberCount) {
        return new TeamResponseDto(
                team.getId(),
                team.getName(),
                team.getStatus().name(),
                team.getInviteCode(),
                memberCount
        );
    }
    //"hahah"

}
