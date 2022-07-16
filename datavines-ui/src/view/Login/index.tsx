import React, { useState } from 'react';
import './index.less';
import {
    Form, Input, Button,
} from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useIntl } from 'react-intl';
import shareData from 'src/utils/shareData';
import { DV_STORAGE_LOGIN } from 'src/utils/constants';
import { useHistory } from 'react-router-dom';
import { SwitchLanguage } from '@/component';
import { $http } from '@/http';
import { useCommonActions } from '@/store';

type TLoginValues = {
    username: string,
    password: string,
}

const Login = () => {
    const { setIsDetailPage } = useCommonActions();
    const [loading, setLoading] = useState(false);
    const [form] = Form.useForm();
    const intl = useIntl();
    const history = useHistory();
    const onFinish = async (values: TLoginValues) => {
        try {
            setLoading(true);
            const res = await $http.post('/login', values, { showWholeData: true });
            shareData.sessionSet(DV_STORAGE_LOGIN, {
                ...(res.data),
                token: res.token,
            });
            setIsDetailPage(false);
            history.push('/main/home');
        } catch (error: any) {
        } finally {
            setLoading(false);
        }
    };
    return (
        <div className="dv-login">
            <div className="dv-login__switch-language"><SwitchLanguage /></div>
            <div className="dv-login-containner">
                <div className="dv-login-wrap">
                    <div className="dv-login-title main-color">DataVines</div>
                    <Form
                        form={form}
                        layout="vertical"
                        name="dv-login"
                        onFinish={onFinish}
                    >
                        <Form.Item
                            label={<span style={{ fontSize: 14 }}>{intl.formatMessage({ id: 'userName_text' })}</span>}
                            name="username"
                            style={{ marginBottom: 15 }}
                            rules={[{ required: true, message: intl.formatMessage({ id: 'login_username_msg' }) }]}
                        >
                            <Input style={{ height: 50 }} size="large" prefix={<UserOutlined />} />
                        </Form.Item>

                        <Form.Item
                            label={<span style={{ fontSize: 14 }}>{intl.formatMessage({ id: 'password_text' })}</span>}
                            name="password"
                            style={{ marginBottom: 15 }}
                            rules={[{ required: true, message: intl.formatMessage({ id: 'login_password_msg' }) }]}
                        >
                            <Input.Password style={{ height: 50 }} size="large" prefix={<LockOutlined />} />
                        </Form.Item>
                        <Form.Item>
                            <Button loading={loading} style={{ width: '100%' }} size="large" type="primary" htmlType="submit">
                                {intl.formatMessage({ id: 'login_btn_text' })}
                            </Button>
                        </Form.Item>
                        <div className="dv-flex-between">
                            <span />
                            {/* <a href="#/forgetPwd">{intl.formatMessage({ id: 'forget_password' })}</a> */}
                            <a href="#/register">{intl.formatMessage({ id: 'register' })}</a>
                        </div>
                    </Form>
                </div>
            </div>
        </div>
    );
};
export default Login;
