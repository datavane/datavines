

#### switch Intl
```js
import { useIntl } from 'react-intl';

const intl = useIntl();
intl.formatMessage({
  id: 'foo',
  defaultMessage: 'Hello',
})
```

TDL
1、右侧内容展示区域，中间可滚动，背景为白色



<a
                            style={{
                                minWidth: 70,
                                display: 'inline-block',
                                width: verificationCodeText.length * 7,
                            }}
                            onClick={
                                getVerificationCode
                            }
                        >
                            {hasRemaining && `${remaining}S`}
                            {!hasRemaining && getVerificationCodeText}
                        </a>