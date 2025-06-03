import React from 'react'
import classNames from 'classnames/bind'
import styles from './Avatar.module.scss'

const cx = classNames.bind(styles);

function Avatar(props) {
    const { className, fallBack, src } = props;
    const classes = cx('default', className)
    const [fallback, setFallBack] = useState('');


    return (
        <img
            src=""
            alt=""
            onError={ () => handleChangeAvt() }
        />
    )
}

export default Avatar