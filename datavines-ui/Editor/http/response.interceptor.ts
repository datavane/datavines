/* eslint-disable prefer-promise-reject-errors */
import { IResponseInterface } from './type';

// Current locale for i18n error messages
let currentLocale = 'en_US';

// Set locale for error messages
export const setHttpErrorLocale = (locale: string) => {
    currentLocale = locale;
};

// i18n error messages
const errorMessages: Record<string, Record<string, string>> = {
    zh_CN: {
        network: '网络连接失败，无法连接到服务器',
        timeout: '请求超时，请检查网络后重试',
        400: '请求参数错误',
        401: '未授权，请先登录',
        403: '访问被拒绝',
        404: '资源不存在',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务暂时不可用',
        504: '网关超时',
        unknown: '服务器错误',
    },
    en_US: {
        network: 'Network connection failed, unable to connect to server',
        timeout: 'Request timeout, please check your network',
        400: 'Bad request',
        401: 'Unauthorized, please login',
        403: 'Access denied',
        404: 'Resource not found',
        500: 'Internal server error',
        502: 'Gateway error',
        503: 'Service unavailable',
        504: 'Gateway timeout',
        unknown: 'Server error',
    },
};

const getErrorMessage = (key: string): string => {
    const messages = errorMessages[currentLocale] || errorMessages.en_US;
    return messages[key] || messages.unknown;
};

export const responseOnSuccess = (response: IResponseInterface) => {
    const { data } = response;
    if (data) {
        if (data instanceof Blob) {
            return {
                success: true,
                data,
            };
        }
        if (data.code === 200) {
            return data;
        }
    }
    return Promise.reject({
        msg: data?.msg,
        code: data?.code,
    });
};

export const responseOnError = (error: any): any => {
    console.log('Response error:', error);
    // eslint-disable-next-line no-underscore-dangle
    if (error?.__CANCEL__) {
        return;
    }

    // Server returned HTTP error (e.g., 500)
    if (error.response) {
        const { status, data } = error.response;

        // Prefer backend error message if available
        if (data && data.msg) {
            return Promise.reject({
                msg: data.msg,
                code: data.code || status,
            });
        }

        // HTTP status code error without structured message
        return Promise.reject({
            msg: getErrorMessage(String(status)),
            code: status,
        });
    }

    // Timeout error
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        return Promise.reject({
            msg: getErrorMessage('timeout'),
            code: 'TIMEOUT',
        });
    }

    // Network error (unable to connect to server)
    return Promise.reject({
        msg: getErrorMessage('network'),
        code: 'NETWORK_ERROR',
    });
};
