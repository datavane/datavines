import { createRoot } from 'react-dom/client';
import { Inspector } from 'react-dev-inspector';
import React from 'react';
import App from './App';
import './global.less';

const InspectorWrapper = process.env.NODE_ENV === 'development' ? Inspector : React.Fragment;
const container = document.getElementById('root') as HTMLElement;
const root = createRoot(container!);
root.render(<InspectorWrapper keys={['control', 'shift', 'command', 'c']}><App /></InspectorWrapper>);
