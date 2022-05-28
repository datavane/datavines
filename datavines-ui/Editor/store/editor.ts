import { IMonacoConfig, IDvDataBaseItem } from '../type';
import createStore from './createStore';

type TEditor = {
    monacoConfig: IMonacoConfig,
    id: string | number | null,
    headers: Record<string, any>,
    databases: IDvDataBaseItem[],
    baseURL: string,
}

const initState: TEditor = {
    monacoConfig: { paths: { vs: '' } },
    headers: {},
    databases: [],
    baseURL: '',
    id: null,
};

const reducer = (state: TEditor, action: any) => ({
    ...state,
    ...action.payload,
});

// @ts-ignore
const setEditorFn = ({ dispatch, getState }) => (value) => {
    console.log('next state', { ...getState(), ...value });
    dispatch({ payload: { ...value } });
};

// @ts-ignore
const clearEditorDataFn = ({ dispatch }) => () => {
    dispatch({ payload: { databases: [] } });
};

const {
    useContextState: useEditorContextState, useActions: useEditorActions, Provider: EditorProvider,
} = createStore<TEditor>(reducer, initState);

export {
    useEditorContextState, useEditorActions, EditorProvider, setEditorFn, clearEditorDataFn,
};
