import React, { useState, useEffect } from 'react';
import {
    Modal, Form, Select, Divider, Button, Space, Typography, message,
} from 'antd';
import { PlusOutlined, MinusCircleOutlined } from '@ant-design/icons';
import { useIntl } from 'react-intl';
import useRequest from '../../hooks/useRequest';
import { CatalogEntityInstanceInfo } from './useLineageData';

const { Text } = Typography;

interface AddLineageModalProps {
    visible: boolean;
    onClose: () => void;
    onSuccess: () => void;
    datasourceList: any[];
}

const AddLineageModal: React.FC<AddLineageModalProps> = ({
    visible, onClose, onSuccess, datasourceList,
}) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);

    // Cascade data
    const [fromDatabases, setFromDatabases] = useState<any[]>([]);
    const [fromTables, setFromTables] = useState<any[]>([]);
    const [fromColumns, setFromColumns] = useState<any[]>([]);
    const [toDatabases, setToDatabases] = useState<any[]>([]);
    const [toTables, setToTables] = useState<any[]>([]);
    const [toColumns, setToColumns] = useState<any[]>([]);

    const loadDatabases = async (dsUuid: string, setFn: (v: any[]) => void) => {
        try {
            const res = await $http.get(`/catalog/list/database/${dsUuid}`);
            setFn(res || []);
        } catch { setFn([]); }
    };

    const loadTables = async (dbUuid: string, setFn: (v: any[]) => void) => {
        try {
            const res = await $http.get(`/catalog/list/table/${dbUuid}`);
            setFn(res || []);
        } catch { setFn([]); }
    };

    const loadColumns = async (tableUuid: string, setFn: (v: any[]) => void) => {
        try {
            const res = await $http.get(`/catalog/list/column/${tableUuid}`);
            setFn(res || []);
        } catch { setFn([]); }
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            setSubmitting(true);

            const columnMappings = values.columnMappings || [];
            const childRelDetailList = columnMappings
                .filter((m: any) => m?.fromColumn && m?.toColumn)
                .map((m: any) => ({
                    fromChildren: [{ uuid: m.fromColumn }],
                    toChild: { uuid: m.toColumn },
                }));

            const payload = {
                fromEntity: { uuid: values.fromTable },
                toEntity: { uuid: values.toTable },
                lineageDetail: {
                    sourceType: 'MANUAL',
                    childRelDetailList,
                },
            };

            await $http.post('/catalog/lineage/add', payload);
            message.success(intl.formatMessage({ id: 'lineage_add' }) + ' ✓');
            form.resetFields();
            onSuccess();
        } catch (e: any) {
            if (e?.msg) message.error(e.msg);
        } finally {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        if (!visible) {
            form.resetFields();
            setFromDatabases([]);
            setFromTables([]);
            setFromColumns([]);
            setToDatabases([]);
            setToTables([]);
            setToColumns([]);
        }
    }, [visible]);

    return (
        <Modal
            title={intl.formatMessage({ id: 'lineage_add' })}
            open={visible}
            onCancel={onClose}
            onOk={handleSubmit}
            confirmLoading={submitting}
            width={600}
            destroyOnClose
        >
            <Form form={form} layout="vertical">
                <Text strong>{intl.formatMessage({ id: 'lineage_source_table' })}</Text>
                <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <Form.Item
                        name="fromDatasource"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_datasource' })}
                            options={(datasourceList || []).map((ds: any) => ({
                                label: ds.name, value: ds.uuid,
                            }))}
                            onChange={(val) => {
                                loadDatabases(val, setFromDatabases);
                                form.setFieldsValue({ fromDatabase: undefined, fromTable: undefined });
                                setFromTables([]);
                                setFromColumns([]);
                            }}
                        />
                    </Form.Item>
                    <Form.Item
                        name="fromDatabase"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_database' })}
                            options={fromDatabases.map((db: any) => ({
                                label: db.name, value: db.uuid,
                            }))}
                            onChange={(val) => {
                                loadTables(val, setFromTables);
                                form.setFieldsValue({ fromTable: undefined });
                                setFromColumns([]);
                            }}
                        />
                    </Form.Item>
                    <Form.Item
                        name="fromTable"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_table' })}
                            options={fromTables.map((t: any) => ({
                                label: t.name, value: t.uuid,
                            }))}
                            onChange={(val) => {
                                loadColumns(val, setFromColumns);
                            }}
                        />
                    </Form.Item>
                </div>

                <Divider style={{ margin: '8px 0' }}>↓</Divider>

                <Text strong>{intl.formatMessage({ id: 'lineage_target_table' })}</Text>
                <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <Form.Item
                        name="toDatasource"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_datasource' })}
                            options={(datasourceList || []).map((ds: any) => ({
                                label: ds.name, value: ds.uuid,
                            }))}
                            onChange={(val) => {
                                loadDatabases(val, setToDatabases);
                                form.setFieldsValue({ toDatabase: undefined, toTable: undefined });
                                setToTables([]);
                                setToColumns([]);
                            }}
                        />
                    </Form.Item>
                    <Form.Item
                        name="toDatabase"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_database' })}
                            options={toDatabases.map((db: any) => ({
                                label: db.name, value: db.uuid,
                            }))}
                            onChange={(val) => {
                                loadTables(val, setToTables);
                                form.setFieldsValue({ toTable: undefined });
                                setToColumns([]);
                            }}
                        />
                    </Form.Item>
                    <Form.Item
                        name="toTable"
                        style={{ flex: 1 }}
                        rules={[{ required: true }]}
                    >
                        <Select
                            placeholder={intl.formatMessage({ id: 'lineage_select_table' })}
                            options={toTables.map((t: any) => ({
                                label: t.name, value: t.uuid,
                            }))}
                            onChange={(val) => {
                                loadColumns(val, setToColumns);
                            }}
                        />
                    </Form.Item>
                </div>

                <Divider style={{ margin: '8px 0' }} />

                <Text strong>{intl.formatMessage({ id: 'lineage_column_mapping' })} ({intl.formatMessage({ id: 'lineage_optional' })})</Text>
                <Form.List name="columnMappings">
                    {(fields, { add, remove }) => (
                        <>
                            {fields.map(({ key, name, ...restField }) => (
                                <div key={key} style={{ display: 'flex', gap: 8, marginTop: 8, alignItems: 'center' }}>
                                    <Form.Item {...restField} name={[name, 'fromColumn']} style={{ flex: 1, marginBottom: 0 }}>
                                        <Select
                                            placeholder={intl.formatMessage({ id: 'lineage_select_column' })}
                                            options={fromColumns.map((c: any) => ({
                                                label: c.name, value: c.uuid,
                                            }))}
                                        />
                                    </Form.Item>
                                    <span>→</span>
                                    <Form.Item {...restField} name={[name, 'toColumn']} style={{ flex: 1, marginBottom: 0 }}>
                                        <Select
                                            placeholder={intl.formatMessage({ id: 'lineage_select_column' })}
                                            options={toColumns.map((c: any) => ({
                                                label: c.name, value: c.uuid,
                                            }))}
                                        />
                                    </Form.Item>
                                    <MinusCircleOutlined onClick={() => remove(name)} />
                                </div>
                            ))}
                            <Button
                                type="dashed"
                                onClick={() => add()}
                                icon={<PlusOutlined />}
                                style={{ marginTop: 8, width: '100%' }}
                                size="small"
                                disabled={fromColumns.length === 0 || toColumns.length === 0}
                            >
                                {intl.formatMessage({ id: 'lineage_column_mapping' })}
                            </Button>
                        </>
                    )}
                </Form.List>
            </Form>
        </Modal>
    );
};

export default AddLineageModal;
