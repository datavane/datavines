import React from 'react';
import ReactDOM from 'react-dom';
import { Inspector } from 'react-dev-inspector';
import App from './App';
import './global.less';
import './preset';

const InspectorWrapper = process.env.NODE_ENV === 'development' ? Inspector : React.Fragment;

ReactDOM.render(
    <InspectorWrapper keys={['control', 'shift', 'command', 'c']}>
        <App />
    </InspectorWrapper>,
    document.getElementById('root'),
);
