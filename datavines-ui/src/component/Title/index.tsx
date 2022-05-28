import React, { FC } from 'react';
import './index.less';

const Title: FC<{}> = ({ children }) => (
    <div className="dv-title">
        {children}
    </div>
);

export default Title;
