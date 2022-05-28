import React from 'react';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/es/locale/zh_CN';
import enUS from 'antd/es/locale/en_US';
import { useSelector } from '@/store';

const ConfigProviderWrap:React.FC<{}> = ({ children }) => {
    const { prefixCls, locale } = useSelector((r) => r.commonReducer);
    const getLocale = () => {
        if (locale === 'zh_CN') {
            return zhCN;
        }
        return enUS;
    };
    return (
        <ConfigProvider locale={getLocale()} prefixCls={prefixCls}>
            {children}
        </ConfigProvider>
    );
};

export {
    ConfigProviderWrap,
};
