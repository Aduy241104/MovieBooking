import styles from './CustomizeText.module.scss';
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

function CustomizeText({ level = 'h1', children, className }) {
    const Tag = level; // level sẽ là 'h1', 'h2', 'h3', ...
    return (
        <Tag className={ cx('customize', className) }>
            <i className="fa-solid fa-circle-play"></i> { children }
        </Tag>
    );
}

export default CustomizeText;
