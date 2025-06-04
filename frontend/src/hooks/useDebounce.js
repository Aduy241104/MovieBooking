import { useEffect, useState } from 'react'

function useDebounce(value, delay) {
    const [debounceValue, setDebounceValue] = useState(value);
    
    useEffect(() => {
        let timout = 0;

        if (!!value.trim()) {
            timout = setTimeout(() => {
                setDebounceValue(value);
            }, delay);
        } else {
            setDebounceValue("");
        }

        return () => clearTimeout(timout);
    }, [value])

    return debounceValue;
}

export default useDebounce