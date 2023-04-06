/* eslint-disable react/no-danger */
import React, { useRef } from 'react';
import { ModalProps, Table } from 'antd';
import {
    useModal, useImmutable, usePersistFn,
} from 'src/common';
import { useIntl } from 'react-intl';

type InnerProps = {
    [key: string]: any
}
type tableItem = {
    [key: string]: any;
}
const Inner = (props: InnerProps) => {
    const intl = useIntl();
    // const [loading, setLoading] = useState(false);
    // const [tableData, setTableData] = useState<any[]>([]);
    // const [pageParams, setPageParams] = useState({
    //     pageNumber: 1,
    //     pageSize: 10,
    // });
    const columns = [{
        title: intl.formatMessage({ id: 'job_column_Name' }),
        dataIndex: 'name',
        key: 'name',
        // render: (_: any, { name }: any) => <span className="text-underline">{name}</span>,
    },
        // {
        //     title: intl.formatMessage({ id: 'warn_sLAs_type' }),
        //     dataIndex: 'type',
        //     key: 'type',

        // },
        // , {
        //     title: intl.formatMessage({ id: 'job_null' }),
        //     dataIndex: 'nullCount',
        //     key: 'nullCount',
        //     render: (_: any, { nullCount, nullPercentage }: any) => (
        //         <span>
        //             {nullCount}
        //             {' '}
        //             [
        //             {nullPercentage}
        //             ]
        //         </span>
        //     ),

        // }, {
        //     title: intl.formatMessage({ id: 'job_notNull' }),
        //     dataIndex: 'notNullCount',
        //     key: 'notNullCount',
        //     render: (_: any, { notNullCount, notNullPercentage }: any) => (
        //         <span>
        //             {notNullCount}
        //             {' '}
        //             [
        //             {notNullPercentage}
        //             ]
        //         </span>
        //     ),

        // }, {
        //     title: intl.formatMessage({ id: 'job_unique' }),
        //     dataIndex: 'uniqueCount',
        //     key: 'uniqueCount',
        //     render: (_: any, { uniqueCount, uniquePercentage }: any) => (
        //         <span>
        //             {uniqueCount}
        //             {' '}
        //             [
        //             {uniquePercentage}
        //             ]
        //         </span>
        //     ),

    // }, {
    //     title: intl.formatMessage({ id: 'job_distinct' }),
    //     dataIndex: 'distinctCount',
    //     key: 'distinctCount',
    //     render: (_: any, { distinctCount, distinctPercentage }: any) => (
    //         <span>
    //             {distinctCount}
    //             {' '}
    //             [
    //             {distinctPercentage}
    //             ]
    //         </span>
    //     ),
    // }
    ];
    // const getData = async () => {
    //     console.log('props', props);
    //     setTableData([]);
    //     try {
    //         setLoading(true);
    //         const { table, id, database } = props.record;
    //         if (!table || !id || !database) return;
    //         const $column = await $http.get(`datasource/${id}/${database}/${table}/columns`);
    //         // console.log('$column', $column);
    //         // eslint-disable-next-line no-unused-expressions
    //         // index === 1 ? setCloumn1($column || []) : setCloumn2($column || []);
    //         setTableData($column);
    //     } catch (error) {
    //     } finally {
    //         setLoading(false);
    //     }
    // };
    // useWatch([pageParams], async () => {
    //     getData();
    // }, { immediate: true });
    // const onChange = ({ current, pageSize }: any) => {
    //     setPageParams({
    //         pageNumber: current,
    //         pageSize,
    //     });
    // };
    return (
        <div>
            <Table<tableItem>
                size="middle"
                rowKey="id"
                columns={columns}
                dataSource={props.record?.list || []}
                scroll={{
                    y: 'calc(100vh - 400px)',
                }}
                pagination={false}
            />
        </div>
    );
};

export const useColModal = (options: ModalProps) => {
    const intl = useIntl();
    const recordRef = useRef<any>();
    const onOk = usePersistFn(() => {
        hide();
    });
    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: `${intl.formatMessage({ id: 'job_column' })}`,
        footer: null,
        width: '40%',
        ...(options || {}),
        afterClose() {
            recordRef.current = null;
        },
        onOk,
    });
    return {
        Render: useImmutable(() => (<Render><Inner record={recordRef.current} /></Render>)),
        show(record: any) {
            recordRef.current = record;
            show(record);
        },
        ...rest,
    };
};
