import React, { useState, useEffect } from 'react';
import {
    Modal, Form, Select, Divider, Typography, message,
} from 'antd';
import { useIntl } from 'react-intl';
import useRequest from '../../hooks/useRequest';

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
    const [toDatabases, setToDatabases] = useState<any[]>([]);
    const [toTables, setToTables] = useState<any[]>([]);

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

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            setSubmitting(true);

            const payload = {
                fromEntity: { uuid: values.fromTable },
                toEntity: { uuid: values.toTable },
                lineageDetail: {
                    sourceType: 'MANUAL',
                    childRelDetailList: [],
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
            setToDatabases([]);
            setToTables([]);
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
                        />
                    </Form.Item>
                </div>
            </Form>
        </Modal>
    );
};

export default AddLineageModal;
