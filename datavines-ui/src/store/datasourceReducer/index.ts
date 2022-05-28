import { Dispatch, Action } from 'redux';
import { useActions } from '../useActions';

type TTableType = 'CARD' | 'TABLE';

export interface DatasourceReducer {
    tableType: TTableType
}

type TAction = {
    type?: 'save_datasource_type',
    payload?: Partial<DatasourceReducer>
}
type TDispatch = Dispatch<Action<TAction['type']>>;

const initialState: DatasourceReducer = {
    tableType: 'CARD',
};

export const datasourceActionsMap = {
    setDatasourceType: (tableType: TTableType) => (dispatch: TDispatch) => {
        dispatch({
            type: 'save_datasource_type',
            payload: tableType,
        });
    },
};

export const useDatasourceActions = () => useActions(datasourceActionsMap);

const datasourceReducer = (state: DatasourceReducer = initialState, action: TAction = {}) => {
    switch (action.type) {
        case 'save_datasource_type':
            return { ...state, tableType: action.payload };
        default:
            return state;
    }
};

export default datasourceReducer;
