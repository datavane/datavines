import React, { memo } from 'react';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';

export default memo(({ style }: { style?: React.CSSProperties}) => {
    const history = useHistory();
    const onClick = () => {
        history.goBack();
    };
    return <a onClick={onClick} style={{ ...(style || {}) }}><ArrowLeftOutlined style={{ fontSize: 16, cursor: 'pointer' }} /></a>;
});
