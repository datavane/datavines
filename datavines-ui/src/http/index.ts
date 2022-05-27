import { getHttp } from '@Editor/http';
import { createHashHistory } from 'history';
import store, { RootReducer } from '@/store';

export const history = createHashHistory();

export const $http = getHttp({
    baseURL: '/api/v1',
    requestInterceptor(config) {
        const { userReducer, commonReducer } = store.getState() as RootReducer;
        const loginInfo = userReducer.loginInfo || {};
        const { locale } = commonReducer;
        if (loginInfo.token) {
            config.headers.Authorization = `Bearer ${loginInfo.token}`;
        }
        if (locale) {
            config.headers.language = locale;
        }
        return config;
    },
    responseInterceptor(response) {
        if (response.data?.code === 10010002) {
            setTimeout(() => {
                window.location.href = '#/login';
            }, 1000);
        }
        return response;
    },
});
