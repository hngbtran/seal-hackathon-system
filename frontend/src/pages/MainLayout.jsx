import { NavLink, Outlet } from "react-router-dom";
import "../css/MainLayout.css";

// Main layout component with header, sidebar, and content area
const MainLayout = () => {
  return (
    <div className="app-layout">
      {/* Header section with logo and user actions */}
      <header className="top-navbar">
        <div className="brand-logo">
          <div className="logo-text-main">HACKATHON</div>
          <div className="logo-text-sub">FPT UNIVERSITY</div>
        </div>

        <div className="user-actions">
          <i className="bi bi-bell-fill icon-bell"></i>
          <div className="avatar-circle">
            <i className="bi bi-person text-primary fs-5"></i>
          </div>
        </div>
      </header>
     
      {/* Main container with sidebar and content area */}
      <div className="main-container">
        <aside className="sidebar">
          <NavLink to="/" className="sidebar-item back-btn mb-3">
            <i className="bi bi-arrow-left"></i>
            <span>Trang chủ</span>
          </NavLink>
          <NavLink to="/user-dashboard" className="sidebar-item">
            <i className="bi bi-flag"></i>
            <span>Cuộc thi</span>
          </NavLink>
          <NavLink to="/event-dashboard" className="sidebar-item mt-2">
            <i className="bi bi-people-fill"></i>
            <span>
              Đội thi
              <br />
              của bạn
            </span>
          </NavLink>
          <NavLink to="/submit" className="sidebar-item mt-2">
            <i className="bi bi-upload"></i>
            <span>
              Nộp bài
              <br />
              dự thi
            </span>
          </NavLink>
        </aside>

        {/* Main content area - renders child routes */}
        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
