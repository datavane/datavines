import React, { memo } from 'react';
import { RollbackOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';

export default memo(() => {
    const history = useHistory();
    const onClick = () => {
        history.goBack();
    };
    return <span onClick={onClick}><RollbackOutlined style={{ fontSize: 20, cursor: 'pointer' }} /></span>;
});
