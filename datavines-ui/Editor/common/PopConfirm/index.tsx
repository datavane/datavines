import React from 'react';
import { useIntl } from 'react-intl';
import { Popconfirm } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';

export default ({ onClick, style }: {onClick?: () => void, style?: React.CSSProperties}) => {
    const intl = useIntl();

    return (
        <Popconfirm
            title={intl.formatMessage({ id: 'common_delete_tip' })}
            onConfirm={() => { onClick?.(); }}
            okText={intl.formatMessage({ id: 'common_Ok' })}
            cancelText={intl.formatMessage({ id: 'common_Cancel' })}
        >
            <a style={{ color: '#f81d22', marginLeft: 10, ...(style || {}) }}><DeleteOutlined /></a>
        </Popconfirm>
    );
};
