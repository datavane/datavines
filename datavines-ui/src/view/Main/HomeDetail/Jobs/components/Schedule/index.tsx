import React from 'react';
import { useIntl } from 'react-intl';
import {
    Input, Form, Radio, DatePicker,
} from 'antd';
import useRequiredRule from '@Editor/hooks/useRequiredRule';
import { CustomSelect } from '@/common';

const Schedule = () => {
    const intl = useIntl();
    const form = Form.useForm()[0];
    const requiredRule = useRequiredRule();
    console.log('1');
    // @ts-ignore
    const Date = <DatePicker />;
    return (
        <Form>
            <Form.Item
                label={intl.formatMessage({ id: 'jobs_schedule_type' })}
                name="name1"
                rules={requiredRule}
                initialValue={undefined}
            >
                <Radio.Group>
                    <Radio value={1}>A</Radio>
                    <Radio value={2}>B</Radio>
                    <Radio value={3}>C</Radio>
                    <Radio value={4}>D</Radio>
                </Radio.Group>
            </Form.Item>

            <Form.Item
                label={intl.formatMessage({ id: 'jobs_schedule_cycle' })}
                name="name2"
                rules={requiredRule}
                initialValue={undefined}
            >
                <CustomSelect source={[]} style={{ width: 240 }} />
            </Form.Item>

            <Form.Item
                label={intl.formatMessage({ id: 'jobs_schedule_time' })}
                name="name3"
                rules={requiredRule}
                initialValue={undefined}
            >
                <CustomSelect source={[]} style={{ width: 240 }} />
            </Form.Item>

            <Form.Item
                label={intl.formatMessage({ id: 'jobs_schedule_express' })}
                name=" "
                rules={requiredRule}
                initialValue={undefined}
            >
                <div style={{ color: '#ff4d4f' }}>自动生成的</div>
            </Form.Item>
            <div>
                <Form.Item
                    label={intl.formatMessage({ id: 'jobs_schedule_effect_time' })}
                    name="time"
                    initialValue={undefined}
                >
                    {Date}
                </Form.Item>
                <Form.Item
                    label=""
                    name="endTime"
                    initialValue={undefined}
                >
                    {Date}
                </Form.Item>
            </div>

        </Form>
    );
};

export default Schedule;
