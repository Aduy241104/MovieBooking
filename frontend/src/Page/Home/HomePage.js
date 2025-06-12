import React, { useContext } from 'react'
import BannerSlide from '../../components/BannerSlide/BannerSlide'
import ComingSoon from '../../components/ComingSoonMovie/ComingSoon'
import PlayingMovie from '../../components/PlayingMovie/PlayingMovie'
import { AuthContext } from '../../context/AuthContext'
import DefaultLayout from '../../layouts/DefaultLayout/DefaultLayout'
import MovieSchedule from '../../components/MovieSchedule/MovieSchedule'

function HomePage() {
  const { logout, token, user } = useContext(AuthContext);

  return (
    <DefaultLayout>
      <BannerSlide></BannerSlide>
      <div className='container-fluid mt-5 ps-5 pe-5'>
        <div className='bg-night rounded-4'>
          <ComingSoon />
          <PlayingMovie />
        </div>
      </div>
      <MovieSchedule />
    </DefaultLayout>

  )
}

export default HomePage