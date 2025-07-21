import { useEffect, useState } from "react"

function SearchHistoryList({ changeSearchValue }) {
  const [listHistory, setListHistory] = useState([]);
  const [hoveredIndex, setHoveredIndex] = useState(null);

  const handleRemoveSearchKeyword = (keyword) => {
    const newListHistory = listHistory.filter(item => item !== keyword);
    localStorage.setItem("searchHistory", JSON.stringify(newListHistory));
    setListHistory(newListHistory);
  }

  useEffect(() => {
    const historyList = JSON.parse(localStorage.getItem('searchHistory')) || [];
    setListHistory(historyList);
  }, []);

  return (
    <div style={ { maxWidth: '260px' } }>
      <p className="text-light fs-8 fw-bold pb-2">Lịch sử tìm kiếm</p>
      <div>
        { listHistory.map((item, index) => {
          return (
            <div
              key={ index }
              className="w-100 rounded-2 text-light p-2 fs-6 gray-hover rounded-0 text-start d-flex align-items-center"
              onMouseEnter={ () => setHoveredIndex(index) }
              onMouseLeave={ () => setHoveredIndex(null) }
            >
              <div
                onClick={ () => changeSearchValue(item) }
                className="flex-1 cursor-pointer"
              >
                <i className="fa-solid fa-clock-rotate-left pe-2 fs-7"></i>
                <span>{ item }</span>
              </div>

              { hoveredIndex === index && (
                <button
                  onClick={ () => handleRemoveSearchKeyword(item) }
                  className="text-end"
                  style={ {
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer'
                  } }
                >
                  <i className="fa-solid fa-xmark"></i>
                </button>
              ) }
            </div>
          );
        }) }
      </div>
    </div>
  );
}

export default SearchHistoryList;
