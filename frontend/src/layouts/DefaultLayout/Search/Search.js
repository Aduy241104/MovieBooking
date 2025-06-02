import Tippy from '@tippyjs/react/headless'
import { useState } from 'react'
import 'tippy.js/dist/tippy.css'

function Search({ children }) {
    const [isShow, setShow] = useState(false);
    return (
        <Tippy
            interactive={ true }
            visible={ isShow }
            placement="top-end"
            offset={[0,15]}
            onClickOutside={ () => setShow(false) }
            render={ attrs => (
                <div
                    className="bg-light p-3 shadow border border-secondary rounded-1"
                    tabIndex="-1"
                    { ...attrs }
                >
                    <h1 className='text-danger'>Hello sxsssss</h1>
                    <h1 className='text-danger'>Hello</h1>
                    <h1 className='text-danger'>Hello</h1>
                </div>
            ) }
        >
            <button onClick={ () => setShow(true) } className="bg-transparent me-3 border-0">
                <i className="fa-solid fa-magnifying-glass text-light"></i>
            </button>
        </Tippy>
    )
}

export default Search