import React, { useRef, useState, useImperativeHandle } from 'react';
import {
    Input, ModalProps, Form, FormInstance, message, DatePicker,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, FormRender, IFormRender, usePersistFn, useLoading,
} from '@/common';
import { $http } from '@/http';
import { useSelector } from '@/store';
import { TTokenTableItem } from "@/type/token";
import dayjs from "dayjs";

type InnerProps = {
    form: FormInstance,
    detail?: TTokenTableItem | null,
    innerRef?: any
}
export const CreateTokenComponent = ({ form, detail, innerRef }: InnerProps) => {
    const intl = useIntl();
    const setBodyLoading = useLoading();
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const schema: IFormRender = {
        name: 'token-form',
        layout: 'vertical',
        formItemProps: {
            style: { marginBottom: 10 },
        },
        meta: [
            {
                label: intl.formatMessage({ id: 'token_expire_time' }),
                name: 'expireTime',
                initialValue: dayjs(detail?.expireTime),
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <DatePicker showTime/>,
            }
        ],
    };
    useImperativeHandle(innerRef, () => ({
        saveUpdate(hide?: () => any) {
            form.validateFields().then(async (values) => {
                try {
                    setBodyLoading(true);
                    const { expireTime } = values;
                    console.log('expireTime：', expireTime);
                    const params = {
                        workspaceId,
                        expireTime: dayjs(expireTime).format('YYYY-MM-DD HH:mm:ss'),
                    };

                    console.log('params：', params);

                    if (detail && detail.id) {
                        await $http.put('/token', { ...params, id: detail.id });
                    } else {
                        await $http.post('/token', params);
                    }
                    message.success(intl.formatMessage({ id: 'common_success' }));
                    if (hide) {
                        hide();
                    }
                } catch (error) {
                } finally {
                    setBodyLoading(false);
                }
            }).catch((err) => {
                console.log(err);
            });
        },
    }));
    return <FormRender {...schema} form={form} />;
};

export const useCreateToken = (options: ModalProps) => {
    const [form] = Form.useForm();
    const intl = useIntl();
    const innerRef = useRef<any>();
    const [editInfo, setEditInfo] = useState<TTokenTableItem | null>(null);
    const editRef = useRef<TTokenTableItem | null>(null);
    editRef.current = editInfo;

    const onOk = usePersistFn(async () => {
        innerRef.current.saveUpdate(hide);
    });
    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: intl.formatMessage({ id: 'create_token' }),
        onOk,
        ...(options || {}),
    });
    return {
        Render: useImmutable(() => (<Render><CreateTokenComponent innerRef={innerRef} form={form} detail={editRef.current} /></Render>)),
        show(data: TTokenTableItem | null) {
            setEditInfo(data);
            show(data);
        },
        ...rest,
    };
};
