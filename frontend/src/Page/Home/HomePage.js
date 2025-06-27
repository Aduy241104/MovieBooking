import BannerSlide from '../../components/BannerSlide/BannerSlide'
import ComingSoon from '../../components/ComingSoonMovie/ComingSoon'
import PlayingMovie from '../../components/PlayingMovie/PlayingMovie'
import DefaultLayout from '../../layouts/DefaultLayout/DefaultLayout'
import MovieSchedule from '../../components/MovieSchedule/MovieSchedule'
import { memo } from 'react'

function HomePage() {
  // const { logout, token, user } = useContext(AuthContext);

  return (
    <DefaultLayout>
      <BannerSlide />
      <div className='container-fluid mt-5 ps-5 pe-5'>
        <div className='bg-night rounded-4'>
          <PlayingMovie />
          <ComingSoon />
        </div>
      </div>
      <MovieSchedule />
    </DefaultLayout>

  )
}

export default memo(HomePage)