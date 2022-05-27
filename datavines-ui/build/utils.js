/* eslint-disable no-loop-func */
/* eslint-disable guard-for-in */
/* eslint-disable no-restricted-syntax */
const path = require('path');
const os = require('os');

const resolve = (str) => path.resolve(__dirname, '..', str);

const getLocalIpSync = () => {
    let host = '127.0.0.1';
    const ifaces = os.networkInterfaces();
    for (const dev in ifaces) {
        ifaces[dev].forEach((details) => {
            if (details.family === 'IPv4' && details.address.indexOf('192.168') >= 0) {
                host = details.address;
            }
        });
    }

    return host;
};

module.exports = {
    resolve,
    host: getLocalIpSync(),
};
