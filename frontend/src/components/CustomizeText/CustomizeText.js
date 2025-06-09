import React from 'react'
import styles from './CustomizeText.module.scss'
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

function CustomizeText({ level = 'h1', children }) {
    const Tag = level; // level sẽ là 'h1', 'h2', 'h3', ...
    return (
        <Tag className={ cx('customize') }><i className="fa-brands fa-staylinked"></i> { children }</Tag>
    )
}

export default CustomizeText