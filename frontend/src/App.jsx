import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './pages/MainLayout';
import UserDashboard from './pages/UserDashboard';
import EventDashboard from './pages/EventDashboard';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          
          <Route index element={<Navigate to="/user-dashboard" replace />} />
          
          <Route path="user-dashboard" element={<UserDashboard />} />
          <Route path="event-dashboard" element={<EventDashboard />} />
          
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;