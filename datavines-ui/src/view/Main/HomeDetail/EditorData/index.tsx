import React, { useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { DvEditor } from '@Editor/index';
import { useSelector } from '@/store';

const EditorData = () => {
    const params = useParams<{ id: string}>();
    const { loginInfo } = useSelector((r) => r.userReducer);
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const { locale } = useSelector((r) => r.commonReducer);
    const editorParams = useMemo(() => ({
        workspaceId,
        monacoConfig: { paths: { vs: '/monaco-editor/min/vs' } },
        baseURL: '/api/v1',
        headers: {
            Authorization: `Bearer ${loginInfo.token}`,
        },
    }), [workspaceId]);
    return (
        <div style={{ height: 'calc(100vh - 60px)', background: '#fff' }}>
            <DvEditor {...editorParams} locale={locale} id={params.id} />
        </div>
    );
};

export default EditorData;
