/* eslint-disable camelcase */
import React, { useState } from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, FormInstance, Input,
} from 'antd';
import { CustomSelect, useMount } from '../../../common';
import Title from '../Title';
import { layoutItem } from '../helper';
import useRequest from '../../../hooks/useRequest';
import { TDetail } from '../type';
import { useEditorContextState } from '../../../store/editor';
import { Switch } from 'antd';
import useRequiredRule from '../../../hooks/useRequiredRule';


type InnerProps = {
    form: FormInstance,
    detail: TDetail,
}

const Index = ({ form, detail }: InnerProps) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [context] = useEditorContextState();
    const [errorList, setErrorList] = useState([]);
    const requiredRules = useRequiredRule();
    const [isErrorDataOutputDataSource, setErrorDataOutputDataSource] = useState(false);
    useMount(async () => {
        try {
            const res = await $http.get(`/errorDataStorage/list/${context.workspaceId}`);
            setErrorList(res || []);
            if (detail && detail.id) {
                form.setFieldsValue({
                    errorDataStorageId: detail?.errorDataStorageId || undefined,
                    isErrorDataOutputToDataSource: detail?.isErrorDataOutputToDataSource || false,
                    errorDataOutputToDataSourceDatabase: detail?.errorDataOutputToDataSourceDatabase || undefined,
                });
            }
        } catch (error) {
        }
    });

    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_error_store_config' })}>
            <Row gutter={30}>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_error_output_to_datasource' })}
                        name="isErrorDataOutputToDataSource"
                    >
                        <Switch checked={isErrorDataOutputDataSource}/>
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item noStyle dependencies={['isErrorDataOutputToDataSource']}>
                        {() => {
                            const value = form.getFieldValue('isErrorDataOutputToDataSource');
                            if (value) {
                                setErrorDataOutputDataSource(true);
                                return (
                                    <Form.Item
                                        {...layoutItem}
                                        rules={requiredRules}
                                        label={intl.formatMessage({ id: 'dv_metric_error_output_to_datasource_database' })}
                                        name="errorDataOutputToDataSourceDatabase"
                                    >
                                        <Input autoComplete="off" allowClear />
                                    </Form.Item>
                                );
                            } else {
                                setErrorDataOutputDataSource(false);
                                return (
                                    <Form.Item
                                        {...layoutItem}
                                        label={intl.formatMessage({ id: 'dv_metric_error_store_engine' })}
                                        name="errorDataStorageId"
                                    >
                                        <CustomSelect allowClear source={errorList} sourceValueMap="id" sourceLabelMap="name" />
                                    </Form.Item>
                                )
                            }
                        }}
                    </Form.Item>

                </Col>
            </Row>
        </Title>
    );
};

export default Index;
