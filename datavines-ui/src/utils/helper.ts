import moment from 'moment';

export const defaultTime = (text: number[], format?: string) => {
    if (!text || text.length <= 0) {
        return '--';
    }
    const $format = format || 'YYYY-MM-DD HH:mm:ss';
    const t1 = text.slice(0, 3).join('-');
    const t2 = text.slice(3).join(':');
    return moment(`${t1} ${t2}`).format($format);
};
