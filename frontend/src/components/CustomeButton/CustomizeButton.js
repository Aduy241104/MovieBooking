import classNames from 'classnames/bind'
import styles from './Customize.module.scss'
import { Link } from 'react-router-dom';

const cx = classNames.bind(styles);

function CustomizeButton({ to, href, onClick, className, children, gold, primary = true,
    outLine, leftIcon, rightIcon, small, large, rounded, roundedBig, ...passProps }) {

    let Comp = 'button';
    let classes = cx('wrapper', {
        [className]: className,
        primary,
        outLine,
        gold,
        small,
        large,
        rounded,
        roundedBig
    }) 

    const props = {
        onClick,
        ...passProps,
    };

    if (to) {
        props.to = to;
        Comp = Link;

    } else if (href) {
        props.href = href;
        Comp = 'a';
    }

    return (
        <Comp className={ classes } { ...props }>
            { leftIcon && <span className={ cx('icon') }>{ leftIcon }</span> }
            <span> { children }</span>
            { rightIcon && <span className={ cx('icon') }>{ rightIcon }</span> }
        </Comp>
    )
}

export default CustomizeButton