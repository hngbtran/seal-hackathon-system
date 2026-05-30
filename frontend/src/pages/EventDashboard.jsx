import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Form,
  Pagination,
  Badge,
} from "react-bootstrap";
import "../css/EventDashboard.css";

const EventDashboard = () => {
  const teams = Array(5).fill({
    name: "Tên đội",
    members: "03/04",
    desc: "Giới thiệu ngắn về đội của bạn và định hướng giải quyết bài toán. Giới thiệu ngắn về đội của bạn và định hướng giải quyết bài toán.",
  });

  return (
    <Container fluid className="pt-2 pb-5">
      <div className="dashboard-content">
        <Card className="team-banner text-white border-0 mb-4 shadow-sm">
          <Card.Body className="d-flex flex-column flex-md-row justify-content-between align-items-md-center p-4 gap-3">
            <div className="d-flex align-items-center gap-3">
              <div
                className="bg-white bg-opacity-25 rounded-circle d-flex justify-content-center align-items-center"
                style={{ width: "50px", height: "50px" }}
              >
                <i className="bi bi-vinyl-fill fs-3 text-white"></i>
              </div>
              <div>
                <h4 className="mb-1 fw-bold fs-4">Bạn chưa có đội</h4>
                <p className="mb-0 small" style={{ opacity: 0.9 }}>
                  Bạn có thể tìm kiếm các đội phù hợp và gửi yêu cầu tham gia.
                  <br />
                  Ngoài ra, bạn có thể tự tạo team hoặc nhập mã mời vào đội từ
                  bạn bè.
                </p>
              </div>
            </div>

            <div className="d-flex flex-row flex-md-column gap-2">
              <Button
                className="fw-bold rounded-pill px-4 py-2 shadow-sm d-flex align-items-center justify-content-center gap-2"
                style={{
                  backgroundColor: "white",
                  color: "#0047df",
                  border: "none",
                }}
              >
                <i className="bi bi-search"></i> Tạo đội
              </Button>
              <Button
                variant="outline-light"
                className="fw-bold rounded-pill px-4 py-2 d-flex align-items-center justify-content-center gap-2"
              >
                <i className="bi bi-keyboard"></i> Nhập mã mời
              </Button>
            </div>
          </Card.Body>
        </Card>

        <Row className="g-4">
          <Col lg={8}>
            <Card className="border-0 shadow-sm rounded-4 mb-4 bg-white">
              <Card.Body className="p-4 pt-4">
                <div className="search-wrapper mb-3 d-flex align-items-center pe-2">
                  <Form.Control
                    placeholder="Tìm kiếm tên đội"
                    className="ps-4 py-2"
                  />
                  <Button className="text-fpt-blue bg-white fs-5 p-1 px-2">
                    <i className="bi bi-search"></i>
                  </Button>
                </div>

                <Form.Check
                  type="checkbox"
                  label="Sinh viên Đại học FPT HCMC"
                  className="text-fpt-blue fw-medium small mb-4 ms-2"
                />

                <Row className="g-3">
                  {teams.map((team, idx) => (
                    <Col md={6} key={idx}>
                      <Card className="h-100 team-card-bg rounded-4 p-2 shadow-sm border-0">
                        <Card.Body className="d-flex flex-column px-3 py-3">
                          <h5 className="team-card-title fw-bold fst-italic mb-2">
                            {team.name}
                          </h5>
                          <Badge
                            bg="white"
                            text="secondary"
                            className="border border-secondary-subtle rounded-pill align-self-start mb-3 fw-medium px-3 py-1"
                          >
                            {team.members} thành viên
                          </Badge>

                          <div className="avatar-group mb-3">
                            <div className="avatar-wrapper">
                              <i className="bi bi-emoji-smile-fill avatar-icon active"></i>
                              <i className="bi bi-moon-stars-fill crown-icon"></i>
                            </div>
                            <div className="avatar-wrapper">
                              <i className="bi bi-emoji-neutral-fill avatar-icon inactive"></i>
                            </div>
                            <div className="avatar-wrapper">
                              <i className="bi bi-emoji-neutral-fill avatar-icon inactive"></i>
                            </div>
                            <div className="avatar-placeholder">+</div>
                          </div>

                          <div className="mb-4">
                            <span
                              className="fw-bold text-fpt-blue"
                              style={{ fontSize: "0.85rem" }}
                            >
                              Mô tả
                            </span>
                            <br />
                            <span
                              className="text-muted"
                              style={{ fontSize: "0.8rem", lineHeight: "1.4" }}
                            >
                              {team.desc}
                            </span>
                          </div>

                          <div className="text-end mt-auto pt-2">
                            {idx < 2 ? (
                              <Button
                                variant="outline-warning"
                                className="rounded-pill fw-bold px-4 btn-sm border-2"
                              >
                                Hủy yêu cầu
                              </Button>
                            ) : (
                              <Button className="bg-fpt-blue rounded-pill fw-bold px-4 btn-sm border-0 text-white shadow-sm">
                                Xin vào đội{" "}
                                <i className="bi bi-arrow-right ms-1"></i>
                              </Button>
                            )}
                          </div>
                        </Card.Body>
                      </Card>
                    </Col>
                  ))}
                </Row>

                <div className="d-flex justify-content-center mt-5 mb-2">
                  <Pagination className="custom-pagination mb-0 align-items-center">
                    <Pagination.Item className="page-text">
                      <i className="bi bi-arrow-left me-1"></i> Trước
                    </Pagination.Item>
                    <Pagination.Item active>{1}</Pagination.Item>
                    <Pagination.Item>{2}</Pagination.Item>
                    <Pagination.Item>{3}</Pagination.Item>
                    <span className="text-muted mx-2 fw-bold">...</span>
                    <Pagination.Item>{8}</Pagination.Item>
                    <Pagination.Item className="page-text">
                      Tiếp <i className="bi bi-arrow-right ms-1"></i>
                    </Pagination.Item>
                  </Pagination>
                </div>
              </Card.Body>
            </Card>
          </Col>

          <Col lg={4}>
            <Card className="panel-invite bg-white shadow-sm rounded-4 mb-3">
              <Card.Body className="p-4 d-flex justify-content-between align-items-start">
                <div>
                  <h6 className="text-success fw-bold mb-1 fs-6">
                    <i className="bi bi-envelope me-2"></i> Lời mời vào đội
                  </h6>
                  <small className="text-muted" style={{ fontSize: "0.8rem" }}>
                    Chưa có yêu cầu nào.
                  </small>
                </div>
                <Badge
                  bg="success"
                  pill
                  className="px-3 py-2 fw-bold text-white"
                >
                  0
                </Badge>
              </Card.Body>
            </Card>

            <Card className="panel-request bg-white shadow-sm rounded-4">
              <Card.Body className="p-4 d-flex justify-content-between align-items-start">
                <div>
                  <h6 className="text-warning fw-bold mb-1 fs-6">
                    <i className="bi bi-clock-history me-2"></i> Yêu cầu đã gửi
                  </h6>
                  <small className="text-muted" style={{ fontSize: "0.8rem" }}>
                    Chưa gửi lời mời nào.
                  </small>
                </div>
                <Badge
                  bg="warning"
                  pill
                  className="px-3 py-2 fw-bold text-white"
                >
                  0
                </Badge>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </div>
    </Container>
  );
};

export default EventDashboard; // Đã đổi tên export

