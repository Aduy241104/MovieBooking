import React from 'react'
import classNames from 'classnames/bind'
import styles from './Avatar.module.scss'
import { useState } from 'react';

const cx = classNames.bind(styles);

function Avatar(props) {
    const { className, fallBack, src } = props;
    const classes = cx('default', className)
    const [imgSrc, setImgSrc] = useState(src);


    return (
        <img
            alt="avatar"
            className={ classes }
            src={ imgSrc }
            onError={ () => setImgSrc(fallBack) }
        />
    )
}

export default Avatar