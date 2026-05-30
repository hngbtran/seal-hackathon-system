import { Container, Row, Col, Card, Button, Badge } from "react-bootstrap";
import "../css/UserDashboard.css";

const UserDashboard = () => {
  return (
    <Container fluid className="pt-2 pb-5">
      <div className="dashboard-content">
        <h3 className="text-fpt-navy fw-bold fst-italic mb-3 fs-4">
          Hackathon đang diễn ra
        </h3>

        <div className="countdown-banner p-3 mb-4 d-flex justify-content-between align-items-center px-4 shadow-sm">
          <div>
            <h5 className="text-warning fw-bold fst-italic mb-1 fs-5">
              Đóng cổng đăng kí sẽ diễn ra vào 20/07/2026
            </h5>
            <span className="text-muted small">
              Hoặc sớm hơn nếu đủ số lượng đội dự thi. Hiện đã có{" "}
              <strong className="text-warning">45/50</strong> đội đã đăng kí.
            </span>
          </div>
          <div className="d-flex gap-2 text-center">
            {["0", "12", "12", "12"].map((time, idx) => (
              <div key={idx} className="countdown-box text-white">
                <h5 className="fw-bold">{time}</h5>
                <span className="countdown-label text-uppercase">
                  {["Ngày", "Tiếng", "Phút", "Giây"][idx]}
                </span>
              </div>
            ))}
          </div>
        </div>

        <Card className="card-outline-blue shadow-sm rounded-4 overflow-hidden mb-5 p-3 bg-white">
          <Row className="g-4">
            <Col lg={4} className="d-flex flex-column">
              <div className="mock-image-placeholder w-100 mb-3 flex-grow-1"></div>
              <div className="d-flex gap-3">
                <Button
                  variant="primary"
                  className="flex-grow-1 rounded-pill fw-bold py-2 shadow-sm"
                  style={{ backgroundColor: "#0047df", borderColor: "#0047df" }}
                >
                  Tham gia
                </Button>
                <Button
                  variant="outline-primary"
                  className="flex-grow-1 rounded-pill fw-bold py-2 border-2"
                  style={{ color: "#0047df", borderColor: "#0047df" }}
                >
                  Chi tiết thể lệ
                </Button>
              </div>
            </Col>

            <Col lg={8} className="ps-lg-4 py-2">
              <h2 className="text-fpt-blue fw-bold fst-italic mb-3">
                SEAL Hackathon Summer 2026
              </h2>

              <div className="info-pill-container mb-4">
                <div className="info-item">
                  <div className="info-icon">
                    <i className="bi bi-calendar4"></i>
                  </div>
                  <div className="info-text-group">
                    <span className="info-title">Thời gian thi đấu</span>
                    <span className="info-value">20/07/2026 - 21/07/2026</span>
                  </div>
                </div>
                <div className="info-item">
                  <div className="info-icon">
                    <i className="bi bi-people-fill"></i>
                  </div>
                  <div className="info-text-group">
                    <span className="info-title">Số lượng thành viên</span>
                    <span className="info-value">3 - 5 người / đội</span>
                  </div>
                </div>
                <div className="info-item">
                  <div className="info-icon">
                    <i className="bi bi-geo-alt-fill"></i>
                  </div>
                  <div className="info-text-group">
                    <span className="info-title">Địa điểm tổ chức</span>
                    <span className="info-value">Đại học FPT</span>
                  </div>
                </div>
                <div className="info-item">
                  <div className="info-icon">
                    <i className="bi bi-trophy-fill"></i>
                  </div>
                  <div className="info-text-group">
                    <span className="info-title">Tổng giá trị giải thưởng</span>
                    <span className="info-value">16.500.000 VNĐ</span>
                  </div>
                </div>
              </div>

              <div className="mb-3">
                <h6 className="text-fpt-navy fw-bold mb-1 fs-6">Chủ đề</h6>
                <p className="text-fpt-blue mb-0 small fw-medium">
                  AI Agents for Software Innovation
                </p>
              </div>

              <div className="mb-4">
                <h6 className="text-fpt-navy fw-bold mb-1 fs-6">Giới thiệu</h6>
                <p
                  className="text-muted small lh-base mb-0"
                  style={{ fontSize: "0.85rem" }}
                >
                  SEAL Hackathon Summer 2026 là sự kiện mở đầu trong hệ thống
                  SEAL – Software Engineering Agile League. Chủ đề mùa Summer
                  2026 là "AI Agents for Software Innovation", nơi sinh viên
                  trải nghiệm áp dụng AI vào vòng đời phát triển phần mềm
                  (SDLC), từ thu thập yêu cầu, thiết kế, phát triển, kiểm thử
                  đến triển khai và giám sát vận hành.
                </p>
              </div>

              <h6 className="text-fpt-navy fw-bold mb-3 mt-4 fs-6">Timeline</h6>
              <div className="custom-timeline">
                <div className="timeline-track"></div>
                <div className="timeline-progress"></div>

                <div className="timeline-node node-success">
                  <div className="node-icon">
                    <i className="bi bi-check-circle"></i>
                  </div>
                  <div className="node-date">08/07/2026</div>
                  <div className="node-desc">
                    Mở cổng
                    <br />
                    đăng kí
                  </div>
                </div>

                <div className="timeline-node node-active">
                  <div className="node-icon">
                    <i className="bi bi-circle-fill"></i>
                  </div>
                  <div className="node-date">20/07/2026</div>
                  <div className="node-desc fw-bold">
                    Đóng cổng
                    <br />
                    đăng kí
                  </div>
                </div>

                <div className="timeline-node node-upcoming">
                  <div className="node-icon"></div>
                  <div className="node-date">28/07/2026</div>
                  <div className="node-desc">
                    Workshop
                    <br />
                    Online
                  </div>
                </div>

                <div className="timeline-node node-upcoming">
                  <div className="node-icon"></div>
                  <div className="node-date">30/07/2026</div>
                  <div className="node-desc">
                    Vòng
                    <br />
                    Sơ khảo
                  </div>
                </div>

                <div className="timeline-node node-upcoming">
                  <div className="node-icon"></div>
                  <div className="node-date">31/07/2026</div>
                  <div className="node-desc">
                    Vòng
                    <br />
                    Chung kết
                  </div>
                </div>
              </div>
            </Col>
          </Row>
        </Card>

        <h3 className="text-fpt-navy fw-bold fst-italic mb-4 fs-4">
          Hackathons bạn tham gia
        </h3>

        <Row className="g-4">
          {[
            {
              title: "SEAL Hackathon Spring\n2026",
              status: "Đang diễn ra",
              variant: "success",
            },
            {
              title: "SEAL Hackathon Spring\n2026",
              status: "Đã diễn ra",
              variant: "warning",
              textDark: true,
            },
            { title: "SEAL Hackathon Spring\n2026", status: "", variant: "" },
          ].map((item, idx) => (
            <Col md={4} lg={3} key={idx}>
              <Card className="card-outline-blue shadow-sm rounded-4 overflow-hidden h-100 bg-white p-2">
                <div className="position-relative">
                  <div className="mock-image-placeholder past-card-img w-100 rounded-3"></div>
                  {item.status && (
                    <Badge
                      bg={item.variant}
                      className={`position-absolute top-0 end-0 m-2 px-3 py-2 fw-bold rounded-pill ${item.textDark ? "text-dark" : ""}`}
                    >
                      {item.status}
                    </Badge>
                  )}
                </div>
                <Card.Body className="px-2 pt-3 pb-2">
                  <Card.Title
                    className="text-fpt-navy fw-bold fst-italic fs-6 mb-0"
                    style={{ whiteSpace: "pre-line" }}
                  >
                    {item.title}
                  </Card.Title>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </Container>
  );
};

export default UserDashboard;

