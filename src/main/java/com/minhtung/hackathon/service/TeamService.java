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

    private final TeamRepository teamRepository ;
    private final MemberRepository memberRepository ;
    private final TeamRequestRepository teamRequestRepository ;
    private final UserRepository userRepository ;
    private final EmailService emailService ;


    @Transactional
    public TeamResponseDto createTeam(CreateTeamDto create ,int leaderId){
        if(create.getTeamName() == null || create.getTeamName().trim().isEmpty()){
            throw new RuntimeException("Tên đội không được để trống") ;
        }
      if(create.getTeamName().length()>30){
          throw new RuntimeException("tên đội không thể lớn hơn 30 kí tự ");
      }

      if(memberRepository.existsByMemberIDAndStatus(leaderId,true)){
          throw new RuntimeException("Bạn thuộc team rồi nên không thể tham gia đăng kí vào team khác") ;
      }

      String inviteCode = generateUniqueInviteCode();
        Team team = new Team(create.getTeamName().trim(),leaderId,inviteCode);
        team.setStatus(TeamStatus.OPEN);
        teamRepository.save(team);

        memberRepository.save(new Member(MemberRole.LEADER,true,team.getId(),leaderId));


        //gửi invitation cho từng email
        List<String> emails = create.getMemberEmails();
        if(emails !=null){
            int limit = Math.min(emails.size(),4) ;
            for (int i = 0 ; i<limit ; i++){
                String email = emails.get(i);

                if(email == null || email.trim().isEmpty()){
                    continue;
                }
                User invitedUsers= userRepository.findByEmail(email.trim()).orElse(null);
                //gui email
                if(invitedUsers == null){
                    emailService.sendTeamInviteEmail(email.trim(),team.getName(),inviteCode);
                    continue;
                }

                //bo qua chinh minh
                if(invitedUsers.getId().equals(leaderId)){
                    continue;
                }
                //luu teamRequest loai invitation(nguoi duoc moi can bam dong y de duoc tham gia vao team
                TeamRequest invination = new TeamRequest(leaderId , invitedUsers.getId(),team.getId(), RequestType.INVITATION);
                teamRequestRepository.save(invination);
                //gui email thong bao
                emailService.sendTeamInviteEmail(email.trim(), team.getName() ,inviteCode);
            }
        }
        int memberCount = memberRepository.countByTeamIdAndStatus((int) team.getId(),true);
        return  toDto(team,memberCount);




        //2. day la ham tham gia doi bang loi moi
        //luat la chi doi dang co Open moi co the join bang ma
        // tu dong them member ma khong can admin duyet

    }
    @Transactional
    public String joinTeamByCode(String inviteCode,int userId) {
        Team team = teamRepository.findByInviteCode(inviteCode).orElse(null);
        if (team == null) {
            return "Mã mời không hợp lệ hoặc không  tồn tại";

        }

        // nhung team nao open thi moi tham gia vao
        if (team.getStatus() != TeamStatus.OPEN) {
            return "đội nay không còn nhận thành viên nữa" + team.getStatus() + ") ";
        }

        //kiem tra xem ban co thuoc team nao chua
        if (memberRepository.existsByTeamIdAndMemberIDAndStatus(team.getId(), userId, true)) {
            ;

            return "ban da thuoc doi khac roi" + team.getName();
        }
        //yeu cau roi doi truoc khi muon tham gia
        if (memberRepository.existsByMemberIDAndStatus(userId,true)) {
            return "ban nen roi doi " + team.getName() + "truoc khi tham gia ";

        }

        int currentCount = memberRepository.countByTeamIdAndStatus(team.getId(),true);
        if(currentCount >=4){
            return "doi da du 4 thanh vien roi" ;
        }
        memberRepository.save(new Member(MemberRole.MEMBER,true, team.getId(),userId ));
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
                int count = memberRepository.countByTeamIdAndStatus(team.getId(), true);
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
        public  String sendJoinRequest(Long teamId,int userId) {
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team == null) {
                return "không tim thấy đội ";
            }
            if (team.getStatus() != TeamStatus.OPEN) {
                return "đội nay không còn nhận thành viên nữa";

            }
            if (memberRepository.existsByTeamIdAndMemberIDAndStatus(teamId, userId, true)) {
                ;
                return "ban da o trong doi khac roi";
            }
            if (memberRepository.countByTeamIdAndStatus(teamId, true) >= 4) {
                return "doi nay da du so luong thanh vien la 4";

            }
            //khuc nay la do leader duyet
            if (teamRequestRepository.existsBySenderIdAndTeamIdAndTypeAndStatus(userId, teamId, RequestType.JOIN_REQUEST, RequestStatus.PENDING)){
                ;
            return " bạn đã  yeu cau  leader duyệt";
        }
            teamRequestRepository.save(new TeamRequest(userId,team.getLeaderID(),teamId,RequestType.JOIN_REQUEST));
            return "da gui yeu cau tham gia doi " + team.getName() +"cho leader duyet" ;


        }
        //User dong ý / từ chối lời mời (invation)
    //nguoi duoc moi co the tu xu ly

    @Transactional
    public String respondToInvitation(Long requestId , boolean acp , int userId){
        TeamRequest req = teamRequestRepository.findById(requestId).orElse(null) ;
        if(req == null){
            return "Không tìm thấy lời mời" ;
        }
        if(!req.getReceiverId().equals(userId)){
            return "ban không phải người nhận lời mời" ;
        }
        if(req.getStatus() !=RequestStatus.PENDING){
            return "loi moi da duoc xu ly roi" ;

        }
        if(acp){
            Team team = teamRepository.findById(req.getTeamId()).orElse(null);
            if(team == null || team.getStatus() != TeamStatus.OPEN){
                req.setStatus(RequestStatus.REJECTED);
                teamRequestRepository.save(req);
            }
            if(memberRepository.countByTeamIdAndStatus(team.getId(), true)>=4){
                req.setStatus(RequestStatus.REJECTED);
                teamRequestRepository.save(req);
                return "doi cua bạn da du va khong con nhan thanh vien nua" ;
            }
            if(memberRepository.existsByMemberIDAndStatus(userId,true)){
                return "ban thuoc team khac roi can out de vao nhom khac" ;
            }
           memberRepository.save(new Member(MemberRole.MEMBER,true,req.getTeamId(),userId));
            req.setStatus(RequestStatus.APPROVED);
        }else{
            req.setStatus(RequestStatus.REJECTED);
        }
        teamRequestRepository.save(req) ;
        return acp ? "Đã tham gia đội thành công!" : "Đã từ chối lời mời";
    }

    //Leader duyet / tu choi join request
    @Transactional
    public String respondToJoinRequest(Long requesrId , boolean acp , int leaderId){
        TeamRequest req = teamRequestRepository.findById(requesrId).orElse(null) ;
        if(req != null){
            return "khong tim thay yeu cau" ;
        }
        if(req.getType() !=RequestType.JOIN_REQUEST){
            return "day khong phai yeu cau xin vao doi " ;
        }
        if(!req.getReceiverId().equals(leaderId)){
            return "ban khong phai leader" ;
        }
        if(req.getStatus()!=RequestStatus.PENDING)
            return "Yeu cau nay da duoc xu ly roi " ;

        if(acp){
            int count = memberRepository.countByTeamIdAndStatus(requesrId,true);
            if(count >=4){
                req.setStatus(RequestStatus.REJECTED);
                teamRequestRepository.save(req);
                return "doi cua ban da du 4 thanh vien roi " ;
            }
            memberRepository.save(new Member(MemberRole.MEMBER,true,req.getTeamId(),req.getSenderId()));
            req.setStatus(RequestStatus.APPROVED);
        }else{
            req.setStatus(RequestStatus.REJECTED);
        }
        teamRequestRepository.save(req) ;
        return acp ? "Đã duyệt thành viên vào đội" : "Đã từ chối yêu cầu";
    }


    //thanh vie  gui leave_request xin roi doi
    //leader can duyet
    //leader khong the tu y roi doi (giai tán nhóm)
    @Transactional
    public String requestLeave(Long teamID , int userId) {
        Member member = memberRepository.findByTeamIdAndMemberID(teamID, userId).orElse(null);
        if (member == null || !member.isStatus()) {
            return "ban khong thuoc doi nay ";
        }
        if (member.getRole() == MemberRole.LEADER) {
            return "ban la leader nen khong the roi doi duoc ";
        }
        Team team = teamRepository.findById(teamID).orElse(null);
        if (team == null) {
            return "khong tim thay doi nay";
        }
        if (team.getStatus() == TeamStatus.APPROVED) {
            return "đội của bạn đã được duyệt rôi , không thể rời đội";
        }
        if (teamRequestRepository.existsBySenderIdAndTeamIdAndTypeAndStatus(userId, teamID, RequestType.LEAVE_REQUEST, RequestStatus.PENDING)) {

            return "bạn đã gửi yêu  cầu xin ròi nhóm , cần được admin duyệt ";
        }
        teamRequestRepository.save(new TeamRequest(userId, team.getLeaderID(), teamID, RequestType.LEAVE_REQUEST));

        return "đã gửi yêu cầu rời đội , chờ leader duyệt ";

    }

        //8 day la ham dung de leadder duyet vuec leave_request trong team

    @Transactional
    public String respondToLeaveRequest(Long requestId , boolean acp , int leaderId){
        TeamRequest req = teamRequestRepository.findById(requestId).orElse(null);
        if(req == null){
            return "không tìm thấy yêu câu rời đội" ;
        }
        if(req.getType() !=  RequestType.LEAVE_REQUEST){
            return "đây không phải yêu cầu rời đội" ;
        }
        if(!req.getReceiverId().equals(leaderId)){
            return "ban khong phai leader cua doi nay" ;
        }
        if(req.getStatus() != RequestStatus.PENDING){
            return "yeu cau nay da duoc xu ly roi" ;
        }
        if(acp){
            Member member = memberRepository.findByTeamIdAndMemberID(req.getTeamId(),req.getSenderId()).orElse(null);
            if(member != null){
                member.setStatus(false);//danh dau da duoc roi doi
                memberRepository.save(member);
            }
            req.setStatus(RequestStatus.APPROVED);
        }else{
            req.setStatus(RequestStatus.REJECTED);
        }
        teamRequestRepository.save(req);
        return acp ? "Đã cho phép thành viên rời đội" : "Đã từ chối yêu cầu rời đội";
    }


    // leader chot doi  gui sumbmission cho ban admin
    //team status open thanh pending -approval
    @Transactional
    public  String submitTeamForApporal(int teamId , long leaderId){
        Team team = teamRepository.findById(leaderId).orElse(null);
        if(team == null){
            return "khong tim thay doi" ;
        }
        if (team.getLeaderID() != leaderId) {
            return "Chỉ Leader mới có quyền chốt đội";
        }
        if(team.getStatus() !=TeamStatus.OPEN){
            return "doi phai o trang thai open thi moi choi duoc ";
        }

        int count = memberRepository.countByTeamIdAndStatus(teamId,true);
        if(count == 4 ){
            return "đội phải đủ 4 người mới được submit" ;
        }

        boolean alreadySumbit = teamRequestRepository.existsBySenderIdAndTeamIdAndTypeAndStatus(leaderId,teamId,RequestType.TEAM_SUBMISSION,RequestStatus.PENDING);

        if(alreadySumbit) {
            return "doi dang cho admin duyet ";
        };
            team.setStatus(TeamStatus.APPROVED);
            teamRepository.save(team);


        teamRequestRepository.save(new TeamRequest((int) leaderId,0,teamId,RequestType.TEAM_SUBMISSION));

        return "da nop roi " + team.getName() + "dang cho admin duyet nha" ;
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
        Team team = teamRepository.findById(req.getTeamId()).orElse(null);
        if(team == null){
            return "khong tim thay doi" ;

        }
        if(approve){
            team.setStatus(TeamStatus.APPROVED);//duyet r nen khong cho khoa nua
            team.setInviteCode(null);//vo hieu hoa ma moi de khong cho ai dung lai ma moi nay
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

}
