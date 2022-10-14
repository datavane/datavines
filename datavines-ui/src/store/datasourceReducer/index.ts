import { Dispatch, Action } from 'redux';
import { useActions } from '../useActions';

type TTableType = 'CARD' | 'TABLE';

export interface DatasourceReducer {
    tableType: TTableType,
    modeType: string
}

type TAction = {
    type?: 'save_datasource_type' | 'save_datasource_modeType',
    payload?: Partial<DatasourceReducer>
}
type TDispatch = Dispatch<Action<TAction['type']>>;

const initialState: DatasourceReducer = {
    tableType: 'CARD',
    modeType: ''
};

export const datasourceActionsMap = {
    setDatasourceType: (tableType: TTableType) => (dispatch: TDispatch) => {
        dispatch({
            type: 'save_datasource_type',
            payload: tableType,
        });
    },
    setDatasourceModeType: (modeType: string) => (dispatch: TDispatch) => {
        dispatch({
            type: 'save_datasource_type',
            payload: modeType,
        });
    },
};

export const useDatasourceActions = () => useActions(datasourceActionsMap);

const datasourceReducer = (state: DatasourceReducer = initialState, action: TAction = {}) => {
    switch (action.type) {
        case 'save_datasource_type':
            return { ...state, tableType: action.payload };
        case 'save_datasource_modeType':
            return { ...state, modeType: action.payload };
        default:
            return state;
    }
};

export default datasourceReducer;
