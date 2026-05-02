import axios from 'axios';
import { ElMessage } from 'element-plus';

const isAuthRequest = (url = '') => url.startsWith('/auth/');
const isCaptchaRequest = (url = '') => url.startsWith('/auth/captcha');
const isProfileTermsRequest = (url = '') => url.startsWith('/profile/terms');

const shouldRedirectToLogin = (status, url = '') => {
  if (status !== 401 && status !== 403) {
    return false;
  }

  if (window.location.pathname === '/login') {
    return false;
  }

  if (isProfileTermsRequest(url)) {
    return false;
  }

  return !isAuthRequest(url);
};

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 此处配置前端 Mock 或 真实后端的代理路径
  timeout: 10000, 
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 携带 Token
    const token = localStorage.getItem('token');
    if (token && !isAuthRequest(config.url)) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request Error: ', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    // 如果返回的状态码不是 200，说明接口有问题（这取决于后端的规范）
    // 假设后端标准返回: { code: 200, message: '', data: {} }
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || 'Error Request');
      
      // 401:未登录;
      if (shouldRedirectToLogin(res.code, response.config?.url)) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
    
    return res.data; // 直接返回核心数据部分
  },
  (error) => {
    console.error('Response Error: ', error);
    
    if (shouldRedirectToLogin(error.response?.status, error.config?.url)) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      window.location.href = '/login';
    }

    if ((isCaptchaRequest(error.config?.url) && window.location.pathname === '/login') || isProfileTermsRequest(error.config?.url)) {
      return Promise.reject(error);
    }

    const msg = error.response?.data?.message || error.message || '网络请求错误';
    ElMessage.error({
      message: msg,
      duration: 5 * 1000
    });
    return Promise.reject(error);
  }
);

export default service;
