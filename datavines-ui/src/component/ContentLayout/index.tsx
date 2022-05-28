import React from 'react';

type TProps = {
    height?: number | string,
    style?: React.CSSProperties
}

const ContentLayout: React.FC<TProps> = ({ height, style, children }) => (
    <div style={{
        height: height || 'calc(100vh - 61px)',
        // padding: '10px 0',
        backgroundColor: '#fff',
        overflowY: 'auto',
        ...(style || {}),
    }}
    >
        {children}
    </div>
);

export default ContentLayout;
