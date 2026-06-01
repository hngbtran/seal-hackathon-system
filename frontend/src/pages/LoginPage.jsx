import { useState } from 'react';
import axios from 'axios';
import styles from './LoginPage.module.css'; // Import dưới dạng module

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    axios
      .post('http://localhost:8080/api/auth/login', { 
        email,
        password,
      })
      .then((response) => {
        const data = response.data;

        if (data.token) {
          localStorage.setItem('accessToken', data.token);
          console.log('Access Token đã lưu:', data.token);
          window.location.href = '/'; 
        } else {
          setError(data.message || 'Đăng nhập thất bại!');
        }
      })
      .catch((err) => {
        console.error(err);
        setError(
          err.response?.data?.message || 
          'Lỗi kết nối server. Vui lòng thử lại!'
        );
      })
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginBox}>
        <h2 className={styles.loginTitle}>Đăng nhập</h2>
        <p className={styles.loginSubtitle}>Chào mừng bạn quay trở lại</p>

        <form onSubmit={handleSubmit} className={styles.loginForm}>
          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Nhập email của bạn"
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>Mật khẩu</label>
            <div className={styles.passwordWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Nhập mật khẩu"
                required
              />
              <button
                type="button"
                className={styles.togglePassword}
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          {error && <p className={styles.errorHover}>{error}</p>}

          <button type="submit" className={styles.loginButton} disabled={loading}>
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>

          <div className={styles.forgotPassword}>
            <a href="#">Quên mật khẩu?</a>
          </div>

          <div className={styles.registerLink}>
            Chưa có tài khoản? <a href="#">Đăng ký ngay</a>
          </div>
        </form>
      </div>    
    </div>
  );
};

export default LoginPage;