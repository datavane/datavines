import React, { FC } from 'react';
import './pageContainer.less';

type TPageContainerProps = {
    footer?: React.ReactNode
}

const PageContainer: FC<TPageContainerProps> = ({ footer, children }) => (
    <>
        <div className="dv-page-container__children">
            {children}
        </div>
        <div className="dv-page-container__footer">
            {footer}
        </div>

    </>
);

export default PageContainer;
