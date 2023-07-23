import React, { useMemo } from 'react';
import { Input, Form, FormInstance } from 'antd';
import { useIntl } from 'react-intl';
import { useUnMount } from '@Editor/common';
import './index.less';

type IndexProps = {
    onSearch: (val: string) => void,
    form?: FormInstance,
    placeholder?: string,
}

const Index = (props: IndexProps) => {
    const { onSearch, placeholder } = props;
    const [form] = (props.form ? useMemo(() => [props.form], [props.form]) : Form.useForm()) as [FormInstance];
    const intl = useIntl();
    const $onSearch = () => {
        const values = form.getFieldsValue();
        onSearch(values);
    };

    useUnMount(() => {
        form.resetFields();
    });

    return (
        <div className="dv-datasource__search">
            <Form form={form}>
                <Form.Item
                    label=""
                    name="name"
                >
                    <Input.Search
                        style={{ width: '100%' }}
                        // @ts-ignore
                        placeholder={placeholder || intl.formatMessage({ id: 'home_search_placeholder' })}
                        onPressEnter={() => {
                            $onSearch();
                        }}
                        autoComplete="off"
                        onSearch={$onSearch}
                    />
                </Form.Item>
            </Form>
        </div>
    );
};

export default Index;
