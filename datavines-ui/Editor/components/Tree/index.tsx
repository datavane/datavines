import React, { useEffect, useState } from 'react';
import {
    Tree, message, Button, Dropdown, Menu, Spin, Tooltip, Input, Form,
} from 'antd';
import {
    DatabaseOutlined, CopyOutlined, DownOutlined, PoweroffOutlined, ReloadOutlined,
} from '@ant-design/icons';
import type { DataNode, EventDataNode, TreeProps } from 'antd/lib/tree';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { useIntl } from 'react-intl';
import { useMetricModal } from '../MetricModal';
import { setEditorFn, useEditorActions, useEditorContextState } from '../../store/editor';
import { usePersistFn } from '../../common';
import { IDvDataBaseItem } from '../../type';
import useTableCloumn from './useTableCloumn';
import './index.less';
import { TDetail } from '../MetricModal/type';
import store from '@/store';
import useRequest from '../../hooks/useRequest';

type TIndexProps = {
    getDatabases: (...args: any[]) => void;
    onShowModal?: (...args: any[]) => any;
}

const Index = ({
    getDatabases, onShowModal,
}: TIndexProps) => {
    const { $http } = useRequest();
    const { Render: RenderModal, show } = useMetricModal();
    const [spinning, setSpinning] = useState(false);
    const [{ databases, id, selectDatabases }] = useEditorContextState();
    const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
    const [defaultSelectId, setDefaultSelectId] = useState<string| number>('');
    const intl = useIntl();
    const fns = useEditorActions({ setEditorFn });
    const $setExpandedKeys = usePersistFn((key: React.Key, isCancel?: boolean) => {
        if (isCancel) {
            setExpandedKeys(expandedKeys.filter((item) => (item !== key)));
            return;
        }
        setExpandedKeys([...expandedKeys, key]);
    });
    const { onRequestTable, onRequestCloumn, onSeletCol } = useTableCloumn({ $setExpandedKeys });

    const [filterData, setFilterData] = useState(databases)

    const renderSingle = (item: IDvDataBaseItem) => ({
        title: <span className="dv-editor-tree-title">{item.name}</span>,
        key: `${item.uuid}@@${item.name}`,
        dataName: item.name,
        type: item.type,
        icon: <DatabaseOutlined />,
        uuid: item.uuid,
        children: (item.children || []).map((tableItem) => ({
            title: (
                <Tooltip title={tableItem.name}>
                    <span className="dv-editor-tree-title">
                        {tableItem.name}
                    </span>
                </Tooltip>
            ),
            key: `${item.uuid}@@${item.name}##${tableItem.uuid}@@${tableItem.name}`,
            dataName: tableItem.name,
            parentName: item.name,
            type: tableItem.type,
            uuid: tableItem.uuid,
            icon: (
                <span
                    onClick={(e) => {
                        e.stopPropagation();
                    }}
                >
                    <CopyToClipboard
                        text={`select * from ${item.name}.${tableItem.name}`}
                        onCopy={() => {
                            message.success('Copy success');
                        }}
                    >
                        <CopyOutlined />
                    </CopyToClipboard>
                </span>
            ),
            children: (tableItem.children || []).map((fieldItem) => ({
                title: (
                    <span className="dv-editor-tree-title" title={fieldItem.name}>
                        {fieldItem.name}
                    </span>
                ),
                className: 'dv-editor-tree-field',
                uuid: fieldItem.uuid,
                key: `${item.uuid}@@${item.name}##${tableItem.uuid}@@${tableItem.name}##${fieldItem.uuid}@@${fieldItem.name}`,
                dataName: fieldItem.name,
                type: fieldItem.type,
            })),
        })),
    });
    const onSelect: TreeProps['onSelect'] = (selectedKeys, e: any) => {
        if (e.node?.selected) return;
        const allData = e.node.key.split('##');
        const allSelectDatabases = allData.map((item:any) => {
            const itemData = item.split('@@');
            return {
                name: itemData[1],
                uuid: itemData[0],
            };
        });
        allSelectDatabases.unshift(selectDatabases[0]);
        // 这里会出现跳数据然后获取下级
        if (e.node.type === 'database') {
            setSpinning(true);
            onRequestTable(e.node.dataName, e.node.uuid, allSelectDatabases, () => {
                // update filter data where selected
                let originJson = JSON.stringify(databases)
                setFilterData(filterDataRecursion(JSON.parse(originJson), searchTerm))
                setSpinning(false);
            });
        } else if (e.node.type === 'table') {
            setSpinning(true);
            onRequestCloumn(e.node.parentName, e.node.dataName, e.node.uuid, allSelectDatabases, () => {
                // update filter data where selected
                let originJson = JSON.stringify(databases)
                setFilterData(filterDataRecursion(JSON.parse(originJson), searchTerm))
                setSpinning(false);
            });
        } else if (e.node.type === 'column') {
            onSeletCol(e.node.dataName, e.node.uuid, allSelectDatabases);
        }
    };

    const onExpand = (expandedKeysValue: React.Key[]) => {
        setExpandedKeys(expandedKeysValue);
    };
    // 右键刷新
    const refresh = async (nodeData: DataNode & {type:string;key:string;dataName:string;parentName?:string}) => {
        await $http.post('catalog/refresh', {
            database: nodeData.parentName || nodeData.dataName,
            datasourceId: id,
            table: nodeData.parentName ? nodeData.dataName : '',
        });
        message.success(intl.formatMessage({ id: 'common_success' }));
    };
    const [treeHeight, setTreeHeight] = useState(0);
    useEffect(() => {
        if (databases.length > 0 && expandedKeys.length === 0) {
            onSelect([], {
                event: 'select',
                selected: false,
                selectedNodes: [],
                node: {
                    type: 'database',
                    dataName: databases[0].name,
                    name: databases[0].name,
                    key: `${databases[0].uuid}@@${databases[0].name}`,
                    uuid: databases[0].uuid,
                } as unknown as unknown as EventDataNode<DataNode>,
                nativeEvent: new MouseEvent('move'),
            });
            setDefaultSelectId(`${databases[0].uuid}@@${databases[0].name}`);
            setExpandedKeys([`${databases[0].uuid}@@${databases[0].name}`]);
            // init filter data
            setFilterData(databases)
        }
        return () => {
            if (databases.length > 0 && expandedKeys.length === 0) {
                setTreeHeight(document.getElementsByClassName('dv-editor-tree_list') && document.getElementsByClassName('dv-editor-tree_list').length > 0
                    ? document.getElementsByClassName('dv-editor-tree_list')[0].scrollHeight
                    : document.documentElement.clientHeight - 63);
            }
        };
    }, [databases]);

    const openData = (node:any) => {
        const index = expandedKeys.indexOf(node.key);
        if (index > -1) {
            expandedKeys.splice(index, 1);
            setExpandedKeys([...expandedKeys]);
        } else {
            setExpandedKeys([...expandedKeys, node.key]);
        }
    };

    const titleRender = (nodeData: any) => (
        nodeData.type !== 'column'
            ? (
                <Dropdown
                    // eslint-disable-next-line react/no-unstable-nested-components
                    overlay={() => (
                        <Menu
                            items={[
                                {
                                    key: 'refresh',
                                    label: (
                                        <span onClick={() => refresh(nodeData)}>
                                            {' '}
                                            {intl.formatMessage({ id: 'job_log_refresh' })}
                                        </span>
                                    ),
                                },
                            ]}
                        />
                    )}
                    trigger={['contextMenu']}
                >
                    <div onClick={() => openData(nodeData)}>
                        {nodeData.title as string}
                    </div>
                </Dropdown>
            ) : <div>{nodeData.title as string}</div>
    );

    const [searchTerm, setSearchTerm] = useState('');

    // 递归过滤 关键词
    const filterDataRecursion = (data: any[], searchTerm: string) => {
        return data.filter((item) => {
            const childrenList = item.children || [];
            // If the current node satisfies the condition, return directly.
            if (item.name.toLowerCase().includes(searchTerm.trim().toLowerCase())) {
                return true;
            }
            // recursively match the child nodes.
            const filterChildrenList = filterDataRecursion(childrenList, searchTerm);
            if (filterChildrenList.length > 0) {
                // update filter children nodes
                item.children = filterChildrenList;
                return true;
            }
            return false;
        });
    };

    // listener input
    useEffect(() => {
        const originJson = JSON.stringify(databases)
        if(searchTerm != ''){
            let filterDatabases = filterDataRecursion(JSON.parse(originJson), searchTerm)
            setFilterData(filterDatabases)
        }else {
            setFilterData(databases)
        }
    }, [searchTerm])

    const [form] = Form.useForm();

    return (
        <div className="dv-editor-tree">
            <div
                className="dv-editor-flex-between"
                style={{
                    alignItems: 'center',
                }}
            >
                <Form form={form}>
                    <Form.Item>
                        <Input placeholder={intl.formatMessage({id: 'common_search'})}
                               onChange={e => setSearchTerm(e.target.value)}
                               allowClear
                        />
                    </Form.Item>
                </Form>
                <span style={{ marginRight: 15 }}>
                    <ReloadOutlined
                        onClick={() => {
                            // refresh search input
                            form.resetFields();
                            setSearchTerm("");

                            getDatabases();
                        }}
                    />

                </span>
            </div>
            {
                defaultSelectId ? (
                    <Spin spinning={spinning} size="small">
                        <div className="dv-editor-tree_list">
                            <Tree
                                showIcon
                                switcherIcon={<DownOutlined style={{ fontSize: '14px', position: 'relative', top: '2px' }} />}
                                onSelect={onSelect}
                                onExpand={onExpand}
                                expandedKeys={expandedKeys}
                                // use filter data gen tree
                                treeData={filterData.map((item) => renderSingle(item))}
                                titleRender={titleRender}
                                defaultSelectedKeys={[defaultSelectId]}
                                height={treeHeight}
                            />
                        </div>

                    </Spin>

                ) : ''
            }

            <RenderModal />
        </div>
    );
};

export default Index;
