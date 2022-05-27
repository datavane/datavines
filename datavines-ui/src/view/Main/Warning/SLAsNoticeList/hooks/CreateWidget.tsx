import React from 'react';
import {
    Input, ModalProps, Form, FormInstance,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, FormRender, IFormRender, usePersistFn,
} from '@/common';

type InnerProps = {
    form: FormInstance | undefined
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();
    const schema: IFormRender = {
        name: 'userForm',
        labelCol: { span: 6 },
        wrapperCol: { span: 18 },
        onFinish(values) {
            console.log('yes', values);
        },
        formItemProps: {
            style: { marginBottom: 10 },
        },
        meta: [
            {
                label: intl.formatMessage({ id: 'warn_SLAs_name' }),
                name: 'name',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <Input />,
            },
            {
                label: intl.formatMessage({ id: 'common_desc' }),
                name: 'desc',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <Input />,
            },
        ],
    };
    return <FormRender {...schema} form={form} />;
};

export const useCreateWidget = (options: ModalProps) => {
    const [form] = Form.useForm();
    const intl = useIntl();
    const onOk = usePersistFn(async () => {
        form.validateFields().then((values) => {
            console.log('values', values);
        }).catch((err) => {
            console.log(err);
        });
    });
    const { Render, ...rest } = useModal<any>({
        title: intl.formatMessage({ id: 'warn_create_widget' }),
        onOk,
        ...(options || {}),
    });
    return {
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        ...rest,
    };
};
